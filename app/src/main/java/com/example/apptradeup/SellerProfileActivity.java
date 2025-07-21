package com.example.apptradeup;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.adapter.ProductAdapter;
import com.example.apptradeup.Product;
import com.example.apptradeup.R;
import com.example.apptradeup.util.GridSpacingItemDecoration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SellerProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar, btnBack;
    private Button btnChat,btnOffer;
    private TextView txtName, txtEmail, txtRating, txtProductCount;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<Product> sellerProducts = new ArrayList<>();
    private String sellerId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_seller_profile); // Giữ nguyên layout cũ, nếu đổi tên thì chỉnh lại

        recyclerView = findViewById(R.id.recyclerSellerProducts);
        imgAvatar = findViewById(R.id.imgSellerAvatar);
        txtName = findViewById(R.id.txtSellerName);
        txtEmail = findViewById(R.id.txtSellerEmail);
        txtRating = findViewById(R.id.txtRating);
        txtProductCount = findViewById(R.id.txtProductCount);
        btnChat = findViewById(R.id.btnChat);
        btnBack = findViewById(R.id.btnBack);


        btnBack.setOnClickListener(v -> finish());

        btnChat.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Bạn cần đăng nhập để chat!", Toast.LENGTH_SHORT).show();
                return;
            }
            String currentUserId = currentUser.getUid();
            if (currentUserId.equals(sellerId)) {
                Toast.makeText(this, "Không thể tự chat với chính mình!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Kiểm tra xem đã có chat chưa
            db.collection("chats")
                    .whereArrayContains("members", currentUserId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        String chatId = null;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                            // Kiểm tra members gồm cả mình và seller
                            java.util.List<String> members = (java.util.List<String>) doc.get("members");
                            if (members != null && members.contains(sellerId)) {
                                chatId = doc.getId();
                                break;
                            }
                        }
                        if (chatId != null) {
                            // Đã có chat, mở luôn
                            openChatActivity(chatId, sellerId);
                        } else {
                            // Chưa có, tạo mới
                            java.util.Map<String, Object> chat = new java.util.HashMap<>();
                            java.util.List<String> members = new java.util.ArrayList<>();
                            members.add(currentUserId);
                            members.add(sellerId);
                            chat.put("members", members);
                            chat.put("lastMessage", "");
                            chat.put("lastTimestamp", System.currentTimeMillis());

                            db.collection("chats")
                                    .add(chat)
                                    .addOnSuccessListener(documentReference -> {
                                        openChatActivity(documentReference.getId(), sellerId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Không thể bắt đầu chat", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi kiểm tra cuộc chat!", Toast.LENGTH_SHORT).show();
                    });
        });

        // Lấy sellerId từ Intent
        sellerId = getIntent().getStringExtra("sellerId");
        if (sellerId == null) {
            Toast.makeText(this, "Không tìm thấy người bán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 1, true));
        adapter = new ProductAdapter(this, sellerProducts);
        recyclerView.setAdapter(adapter);

        loadSellerInfo(sellerId);
        loadSellerProducts(sellerId);
    }

    private void loadSellerInfo(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("display_name");
                        String email = documentSnapshot.getString("email");
                        String avatarUrl = documentSnapshot.getString("profile_pic_url");
                        Double rating = documentSnapshot.getDouble("rating_avg");

                        txtName.setText(name != null ? name : "Không rõ");
                        txtEmail.setText(email != null ? email : "Không rõ");

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Picasso.get().load(avatarUrl).into(imgAvatar);
                        }
                        if (rating != null) {
                            txtRating.setText("\u2B50 " + String.format("%.1f", rating));
                        } else {
                            txtRating.setText("\u2B50 Chưa có đánh giá");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thông tin người bán", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSellerProducts(String userId) {
        db.collection("items")
                .whereEqualTo("sellerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sellerProducts.clear();
                    double totalRating = 0;
                    int totalCount = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        sellerProducts.add(product);

                        // Tính tổng rating và số lượt đánh giá từ tất cả review của sản phẩm này
                        List<Product.Review> reviews = product.getReviews();
                        if (reviews != null) {
                            for (Product.Review review : reviews) {
                                totalRating += review.getRating();
                                totalCount++;
                            }
                        }
                    }
                    txtProductCount.setText("\u2022 " + sellerProducts.size() + " sản phẩm");

                    // Hiển thị rating trung bình
                    if (totalCount > 0) {
                        double avg = totalRating / totalCount;
                        txtRating.setText("\u2B50 " + String.format("%.1f", avg));
                    } else {
                        txtRating.setText("\u2B50 Chưa có đánh giá");
                    }

                    adapter.updateList(new ArrayList<>(sellerProducts));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                });
    }
    private void openChatActivity(String chatId, String otherUserId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", otherUserId); // để load tên/avatar đối phương nếu cần
        startActivity(intent);
    }
}
