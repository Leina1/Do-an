package com.example.apptradeup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentNotification extends AppCompatActivity {

    private TextView tvSuccessTitle, tvOrderInfo, tvPaymentType;
    private Button btnBackToDashboard;
    private ImageView successIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_notification);

        // Ánh xạ view
        tvSuccessTitle = findViewById(R.id.tvSuccessTitle);
        tvOrderInfo = findViewById(R.id.tvOrderInfo);
        tvPaymentType = findViewById(R.id.tvPaymentType);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);
        successIcon = findViewById(R.id.successIcon);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        String result = intent.getStringExtra("result"); // VD: "Thanh toán thành công", "Hủy thanh toán", "Lỗi khi đặt hàng"...
        String orderId = intent.getStringExtra("orderId"); // Có thể truyền từ CheckoutActivity nếu cần
        String totalAmount = intent.getStringExtra("totalAmount");
        String paymentType = intent.getStringExtra("paymentType");

        // Hiển thị tiêu đề trạng thái
        if (result != null) {
            if (result.toLowerCase().contains("thành công")) {
                tvSuccessTitle.setText("THANH TOÁN THÀNH CÔNG");
                successIcon.setImageResource(R.drawable.ic_check_circle);
                // set icon và màu xanh
            } else if (result.toLowerCase().contains("hủy")) {
                tvSuccessTitle.setText("THANH TOÁN ĐÃ HỦY");
            } else {
                tvSuccessTitle.setText("LỖI THANH TOÁN");
            }
        } else {
            tvSuccessTitle.setText("THANH TOÁN");
        }

        // Hiển thị thông tin đơn hàng (nếu có)
        StringBuilder infoBuilder = new StringBuilder();
        if (orderId != null) {
            infoBuilder.append("Mã đơn: #").append(orderId);
        }
        if (totalAmount != null) {
            if (infoBuilder.length() > 0) infoBuilder.append(" - ");
            infoBuilder.append("Tổng tiền: ").append(totalAmount).append(" đ");
        }
        if (infoBuilder.length() > 0) {
            tvOrderInfo.setText(infoBuilder.toString());
        } else {
            tvOrderInfo.setText(""); // hoặc set ẩn nếu không truyền
        }

        // Kiểu thanh toán (nếu có)
        if (paymentType != null) {
            tvPaymentType.setText("Kiểu thanh toán: " + paymentType);
        } else {
            tvPaymentType.setText("");
        }

        // Xử lý nút quay về dashboard/home
        btnBackToDashboard.setOnClickListener(v -> {
            finish();
        });
    }
}
