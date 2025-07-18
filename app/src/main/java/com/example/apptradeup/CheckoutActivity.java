package com.example.apptradeup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.adapter.ItemCartAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class CheckoutActivity extends AppCompatActivity {

    private TextView txtUserName, txtUserPhone, txtUserAddress, txtTotalPrice, txtTotalAmount,txtShippingCost,txtDiscount;
    private RadioGroup radioGroupPayment;
    private RecyclerView recyclerView;
    private Button btnPlaceOrder;

    private List<Product> selectedItems;
    private Map<String, Integer> quantities;
    private String userId;
    private double totalPrice = 0;
    private double shippingCost = 0;
    private double discount = 0;
    private double totalAmount = 0;
    private String selectedPaymentMethod = "Tiền mặt";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Khởi tạo view
        txtUserName = findViewById(R.id.tvUserName);
        txtUserPhone = findViewById(R.id.tvUserPhone);
        txtUserAddress = findViewById(R.id.tvUserAddress);
        txtTotalPrice = findViewById(R.id.tvProductTotalPrice);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        recyclerView = findViewById(R.id.recyclerViewCartItems);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        txtShippingCost= findViewById(R.id.tvShippingCost);
        txtDiscount = findViewById(R.id.tvDiscount);
        txtTotalAmount= findViewById(R.id.tvTotalAmount);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String jsonProducts = intent.getStringExtra("selectedProducts");
        String jsonQuantities = intent.getStringExtra("quantities");
        userId = intent.getStringExtra("userId");

        Type productListType = new TypeToken<List<Product>>(){}.getType();
        Type quantityType = new TypeToken<Map<String, Integer>>(){}.getType();
        selectedItems = new Gson().fromJson(jsonProducts, productListType);
        quantities = new Gson().fromJson(jsonQuantities, quantityType);

        // Hiển thị danh sách sản phẩm
        ItemCartAdapter adapter = new ItemCartAdapter(this, selectedItems, null, userId, true, quantities);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Tính tổng tiền
        totalPrice = 0;
        for (Product p : selectedItems) {
            int quantity = quantities.containsKey(p.getId()) ? quantities.get(p.getId()) : 1;
            totalPrice += p.getPrice() * quantity;
        }
        txtTotalPrice.setText(String.format("%,.0f đ", totalPrice));

        // Khởi tạo phí vận chuyển và giảm giá (có thể chỉnh)
        shippingCost = 20000; // ví dụ phí ship 20k
        discount = 0;         // hoặc đặt giảm giá nếu có

        txtShippingCost.setText(String.format("%,.0f đ", shippingCost));
        txtDiscount.setText("" + String.format("%,.0f đ", discount)); // Hiện dấu trừ cho rõ

        // Tính tổng tiền phải thanh toán
        totalAmount = totalPrice + shippingCost - discount;
        txtTotalAmount.setText(String.format("%,.0f đ", totalAmount));

        // Lấy thông tin người dùng từ Firestore
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        txtUserName.setText("Họ tên: " + doc.getString("display_name"));
                        txtUserPhone.setText("SĐT: " + doc.getString("phone"));
                        txtUserAddress.setText("Địa chỉ: " + doc.getString("address"));
                    }
                });

        // Chọn phương thức thanh toán
        radioGroupPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCash) {
                selectedPaymentMethod = "Tiền mặt";
            } else if (checkedId == R.id.radioBank) {
                selectedPaymentMethod = "Chuyển khoản";
            }
        });

        // Xử lý đặt hàng
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<Map<String, Object>> items = new ArrayList<>();
        for (Product p : selectedItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", p.getId());
            item.put("title", p.getTitle());
            item.put("price", p.getPrice());
            item.put("quantity", quantities.getOrDefault(p.getId(), 1));
            items.add(item);
        }

        Map<String, Object> order = new HashMap<>();
        order.put("userId", userId);
        order.put("items", items);
        order.put("total", totalPrice);
        order.put("paymentMethod", selectedPaymentMethod);
        order.put("timestamp", System.currentTimeMillis());

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi đặt hàng", Toast.LENGTH_SHORT).show();
                });
    }
}
