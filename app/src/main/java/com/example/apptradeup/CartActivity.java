package com.example.apptradeup;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.adapter.ItemCartAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCart;
    private ItemCartAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private TextView totalPriceTextView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImageButton btnBack;
    private CheckBox checkBoxSelectAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        totalPriceTextView = findViewById(R.id.txtTotalPrice);
        btnBack = findViewById(R.id.btnBack);
        checkBoxSelectAll = findViewById(R.id.checkBoxSelectAll);

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        btnBack.setOnClickListener(v -> finish());
        checkBoxSelectAll.setOnClickListener(v -> {
            boolean checked = checkBoxSelectAll.isChecked();
            if (adapter != null) {
                adapter.selectAll(checked);  // gọi hàm trong adapter
            }
            calculateTotal();  // cập nhật tổng tiền
        });
        loadCartItems();
    }

    private void loadCartItems() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> cart = (List<String>) documentSnapshot.get("Cart");

                    if (cart == null || cart.isEmpty()) {
                        Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    productList.clear();

                    for (String productId : cart) {
                        db.collection("items").document(productId).get()
                                .addOnSuccessListener(productSnapshot -> {
                                    if (productSnapshot.exists()) {
                                        Product product = productSnapshot.toObject(Product.class);
                                        product.setId(productSnapshot.getId());
                                        productList.add(product);

                                        // Cập nhật RecyclerView
                                        adapter = new ItemCartAdapter(productList, () -> calculateTotal());
                                        recyclerViewCart.setAdapter(adapter);

                                        calculateTotal();
                                    }
                                });
                    }
                });
    }

    private void calculateTotal() {
        double total = 0;
        for (Product p : adapter.getSelectedProducts()) {
            int quantity = adapter.getQuantityFor(p.getId());
            total += p.getPrice() * quantity;
        }
        totalPriceTextView.setText("Tổng: " + String.format("%,.0f đ", total));
    }
}
