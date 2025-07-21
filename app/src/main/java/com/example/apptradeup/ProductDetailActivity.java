package com.example.apptradeup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.apptradeup.adapter.ImagePagerAdapter;
import com.example.apptradeup.adapter.ReviewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {
    private String itemId;
    private FirebaseFirestore db;
    private String sellerId;
    private ViewPager2 viewPagerImages;
    private ImageView imgSellerAvatar;
    private TextView txtProductTitle, txtProductPrice, txtSold, txtQuatity, txtLocation, txtDescription, txtSellerName, txtUserReviewsLabel;
    private RecyclerView recyclerViewReviews;
    private ImageButton btnChat, btnAddToCart, btnBack;
    private Button btnBuy,btnOffer;

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
        txtUserReviewsLabel = findViewById(R.id.txtUserReviewsLabel); // Để hiện "Chưa có đánh giá nào" nếu cần
        btnBack = findViewById(R.id.btnBack);
        btnChat= findViewById(R.id.btnChat);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuy = findViewById(R.id.btnBuyNow);
        btnOffer= findViewById(R.id.btnOffers);

        // Set LayoutManager cho RecyclerView
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        imgSellerAvatar.setOnClickListener(v -> {
            if (sellerId != null) {
                Intent intent = new Intent(this, SellerProfileActivity.class);
                intent.putExtra("sellerId", sellerId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Đang tải thông tin người bán...", Toast.LENGTH_SHORT).show();
            }
        });
        btnAddToCart.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("Cart", com.google.firebase.firestore.FieldValue.arrayUnion(itemId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AddToCart", "Lỗi khi thêm vào giỏ hàng", e);
                        Toast.makeText(this, "Không thể thêm sản phẩm", Toast.LENGTH_SHORT).show();
                    });
        });
        btnBuy.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            // Load sản phẩm (đảm bảo dữ liệu đã load)
            db.collection("items").document(itemId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Product product = documentSnapshot.toObject(Product.class);
                            if (product != null) {
                                product.setId(itemId); // Đảm bảo có id
                                // Tạo list sản phẩm
                                java.util.List<Product> productList = new java.util.ArrayList<>();
                                productList.add(product);
                                // Số lượng mặc định 1
                                java.util.Map<String, Integer> quantities = new java.util.HashMap<>();
                                quantities.put(itemId, 1);

                                Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
                                intent.putExtra("selectedProducts", new com.google.gson.Gson().toJson(productList));
                                intent.putExtra("quantities", new com.google.gson.Gson().toJson(quantities));
                                intent.putExtra("userId", currentUser.getUid());
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                    });
        });
        btnOffer.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("items").document(itemId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Product product = documentSnapshot.toObject(Product.class);
                            if (product != null) {
                                product.setId(itemId);
                                List<Product> productList = new ArrayList<>();
                                productList.add(product);
                                Map<String, Integer> quantities = new HashMap<>();
                                quantities.put(itemId, 1);

                                Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
                                intent.putExtra("selectedProducts", new com.google.gson.Gson().toJson(productList));
                                intent.putExtra("quantities", new com.google.gson.Gson().toJson(quantities));
                                intent.putExtra("userId", currentUser.getUid());
                                intent.putExtra("fromOffer", true); // <-- Thêm key này
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                    });
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
                            Log.d("ProductDetail", "Seller UserId: " + product.getSellerId());

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

                            // HIỂN THỊ REVIEW
                            List<Product.Review> reviewList = product.getReviews();
                            if (reviewList != null && !reviewList.isEmpty()) {
                                recyclerViewReviews.setAdapter(new ReviewAdapter(this, reviewList));
                                txtUserReviewsLabel.setText("Đánh giá của người dùng");
                            } else {
                                recyclerViewReviews.setAdapter(null);
                                txtUserReviewsLabel.setText("Chưa có đánh giá nào");
                            }

                            // Load thông tin người bán
                            loadSellerInfo(product.getSellerId());
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
