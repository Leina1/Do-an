package com.example.apptradeup;

public class NotificationOrderModel {
    public String orderId;
    public String buyerName, buyerPhone, buyerAddress;
    public String productName;
    public int quantity;
    public double totalPrice;
    public boolean sellerConfirmed;
    public boolean buyerConfirmed;
    public boolean isSellerNow;
    public boolean completed;

    // OFFER SUPPORT
    public String orderType;        // "offer" hoặc "normal"
    public double discount;         // Số tiền được giảm giá nếu là offer
    public double totalAmount;      // Tổng tiền phải trả sau khi giảm
    public boolean offerRejected;   // Người bán đã từ chối offer?

    public NotificationOrderModel(
            String orderId,
            String buyerName,
            String buyerPhone,
            String buyerAddress,
            String productName,
            int quantity,
            double totalPrice,
            boolean sellerConfirmed,
            boolean buyerConfirmed,
            boolean completed,
            boolean isSellerNow,
            String orderType,
            double discount,
            double totalAmount,
            boolean offerRejected
    ) {
        this.orderId = orderId;
        this.buyerName = buyerName;
        this.buyerPhone = buyerPhone;
        this.buyerAddress = buyerAddress;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.sellerConfirmed = sellerConfirmed;
        this.buyerConfirmed = buyerConfirmed;
        this.completed = completed;
        this.isSellerNow = isSellerNow;
        this.orderType = orderType;
        this.discount = discount;
        this.totalAmount = totalAmount;
        this.offerRejected = offerRejected;
    }

    // constructor mặc định cho order thường
    public NotificationOrderModel(
            String orderId,
            String buyerName,
            String buyerPhone,
            String buyerAddress,
            String productName,
            int quantity,
            double totalPrice,
            boolean sellerConfirmed,
            boolean buyerConfirmed,
            boolean completed,
            boolean isSellerNow
    ) {
        this(orderId, buyerName, buyerPhone, buyerAddress, productName, quantity, totalPrice, sellerConfirmed, buyerConfirmed, completed, isSellerNow, "normal", 0, totalPrice, false);
    }
}
