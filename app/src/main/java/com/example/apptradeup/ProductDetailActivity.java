package com.example.apptradeup;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.apptradeup.adapter.ImagePagerAdapter;
import com.example.apptradeup.FragmentHome.FragmentSellerProfile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
public class ProductDetailActivity extends AppCompatActivity {
    private String itemId;
    private FirebaseFirestore db;
    private String sellerId;
    private ViewPager2 viewPagerImages;
    private ImageView imgSellerAvatar;
    private TextView txtProductTitle, txtProductPrice, txtSold, txtQuatity, txtLocation, txtDescription, txtSellerName;
    private RecyclerView recyclerViewReviews;
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Nhận ID sản phẩm từ intent
        itemId = getIntent().getStringExtra("itemId");
        if (itemId == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        viewPagerImages = findViewById(R.id.viewPagerImages);
        txtProductTitle = findViewById(R.id.txtProductTitle);
        txtProductPrice = findViewById(R.id.txtProductPrice);
        txtSold = findViewById(R.id.txtSold);
        txtQuatity = findViewById(R.id.txtQuantity);
        txtLocation = findViewById(R.id.txtLocation);
        txtDescription = findViewById(R.id.txtDescription);
        imgSellerAvatar = findViewById(R.id.imgSellerAvatar);
        txtSellerName = findViewById(R.id.txtSellerName);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        imgSellerAvatar.setOnClickListener(v -> {
            if (sellerId != null) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("openSellerProfile", true);
                intent.putExtra("sellerId", sellerId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Đang tải thông tin người bán...", Toast.LENGTH_SHORT).show();
            }
        });

        // Load chi tiết sản phẩm
        loadProductDetails();
    }

    private void loadProductDetails() {
        db.collection("items").document(itemId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            // Gán dữ liệu sản phẩm vào View
                            txtProductTitle.setText(product.getTitle());
                            txtProductPrice.setText(String.format("%,.0fđ", product.getPrice()));
                            txtDescription.setText(product.getDescription());
                            txtLocation.setText(product.getLocation());
                            txtSold.setText("Đã bán: " + product.getSold());
                            txtQuatity.setText("Số lượng: " + product.getQuantity());

                            // Hiển thị ảnh sản phẩm qua ViewPager2
                            if (product.getImages() != null && !product.getImages().isEmpty()) {
                                ImagePagerAdapter adapter = new ImagePagerAdapter(this, product.getImages());
                                viewPagerImages.setAdapter(adapter);
                                viewPagerImages.setClipToPadding(false);
                                viewPagerImages.setClipChildren(false);
                                viewPagerImages.setOffscreenPageLimit(3);
                                viewPagerImages.getChildAt(0).setOverScrollMode(android.view.View.OVER_SCROLL_NEVER);
                            }

                            // Load thông tin người bán
                            loadSellerInfo(product.getUserId());
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadSellerInfo(String userId) {
        this.sellerId = userId;
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String sellerName = documentSnapshot.getString("display_name");
                        String sellerAvatar = documentSnapshot.getString("profile_pic_url");

                        txtSellerName.setText(sellerName != null ? sellerName : "Không rõ");

                        if (sellerAvatar != null && !sellerAvatar.isEmpty()) {
                            Picasso.get().load(sellerAvatar).into(imgSellerAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải thông tin người bán", Toast.LENGTH_SHORT).show();
                });
    }
}

