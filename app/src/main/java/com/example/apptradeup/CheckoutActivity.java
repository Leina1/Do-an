package com.example.apptradeup;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.adapter.ItemCartAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import android.view.View;
import android.widget.AdapterView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.*;

import com.example.apptradeup.Api.CreateOrder;

import org.json.JSONObject;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

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
    private Spinner spinnerDiscount;
    private boolean isFromOffer = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        isFromOffer = getIntent().getBooleanExtra("fromOffer", false);
        // Khởi tạo view

        spinnerDiscount = findViewById(R.id.spinnerDiscount);
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
        if (!isFromOffer) {
            spinnerDiscount.setVisibility(View.GONE);
            discount = 0;
        } else {
            spinnerDiscount.setVisibility(View.VISIBLE);

            spinnerDiscount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int[] percents = {10, 15, 20};
                    int percent = percents[position];
                    discount = totalPrice * percent / 100.0;
                    txtDiscount.setText(String.format("%,.0f đ", discount));
                    totalAmount = totalPrice + shippingCost - discount;
                    txtTotalAmount.setText(String.format("%,.0f đ", totalAmount));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Nếu không chọn thì discount là 0, cập nhật lại total
                    discount = 0;
                    txtDiscount.setText(String.format("%,.0f đ", discount));
                    totalAmount = totalPrice + shippingCost;
                    txtTotalAmount.setText(String.format("%,.0f đ", totalAmount));
                }
            });
        }
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2553, Environment.SANDBOX);


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
        String totalAmountString = String.format("%.0f", totalAmount);

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
                // Khi chọn Tiền mặt, thông báo và sẽ gọi placeOrder() khi nhấn Đặt hàng
                Toast.makeText(this, "Chọn thanh toán bằng Tiền mặt", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.radioBank) {
                selectedPaymentMethod = "Chuyển khoản";
                // Khi chọn Chuyển khoản, thông báo và tạo mã QR khi nhấn Đặt hàng
                Toast.makeText(this, "Chọn thanh toán bằng Chuyển khoản", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý đặt hàng
        btnPlaceOrder.setOnClickListener(v -> {
            if ("Tiền mặt".equals(selectedPaymentMethod)) {
                placeOrder("Đặt hàng thành công");
            } else if ("Chuyển khoản".equals(selectedPaymentMethod)) {
                CreateOrder orderApi = new CreateOrder();
                try {
                    JSONObject data = orderApi.createOrder(totalAmountString);
                    String code = data.getString("return_code");
                    if (code.equals("1")) {
                        String token = data.getString("zp_trans_token");
                        ZaloPaySDK.getInstance().payOrder(CheckoutActivity.this, token, "demozpdk://app", new PayOrderListener() {
                            @Override
                            public void onPaymentSucceeded(String s, String s1, String s2) {
                                // Chỉ tạo đơn hàng và chuyển màn khi đã lưu xong Firestore!
                                placeOrder("Thanh toán thành công");
                            }
                            @Override
                            public void onPaymentCanceled(String s, String s1) {
                                Intent intent1 = new Intent(CheckoutActivity.this, PaymentNotification.class);
                                intent1.putExtra("result", "Hủy thanh toán");
                                startActivity(intent1);
                            }
                            @Override
                            public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                                Intent intent1 = new Intent(CheckoutActivity.this, PaymentNotification.class);
                                intent1.putExtra("result", "Lỗi thanh toán");
                                startActivity(intent1);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Có thể thông báo lỗi tạo QR ở đây
                }
            }
        });
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    private void placeOrder(String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<Map<String, Object>> items = new ArrayList<>();
        for (Product p : selectedItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", p.getId());
            item.put("title", p.getTitle());
            item.put("price", p.getPrice());
            item.put("quantity", quantities.getOrDefault(p.getId(), 1));
            item.put("sellerId", p.getSellerId());
            item.put("imageUrl", p.getImages().get(0)); // Lấy ảnh đầu tiên, nhớ kiểm tra null!
            items.add(item);
        }
        Map<String, Object> order = new HashMap<>();
        order.put("buyerId", userId); // Đã dùng "buyerId" rồi
        order.put("items", items);
        order.put("discount", discount);
        order.put("total", totalPrice);
        order.put("totalAmount", totalAmount);
        order.put("paymentMethod", selectedPaymentMethod);
        order.put("timestamp", System.currentTimeMillis());
        order.put("completed", false);
        order.put("buyerConfirmed", false);
        order.put("sellerConfirmed", false);
        if (isFromOffer) {
            order.put("orderType", "offer");

        } else {
            order.put("orderType", "normal");
        }

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(docRef -> {
                    Intent intent = new Intent(CheckoutActivity.this, PaymentNotification.class);
                    intent.putExtra("result", "Thanh toán thành công");
                    intent.putExtra("orderId", docRef.getId());
                    intent.putExtra("totalAmount", String.format("%,.0f", totalAmount));
                    intent.putExtra("paymentType", selectedPaymentMethod);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Intent intent = new Intent(CheckoutActivity.this, PaymentNotification.class);
                    intent.putExtra("result", "Lỗi khi đặt hàng");
                    startActivity(intent);
                });
    }


}
