package com.example.apptradeup;

import java.util.List;

public class OrderHistoryModel {
    private String id;
    private String buyerId;
    private String sellerId;
    private boolean completed;
    private boolean buyerConfirmed;
    private boolean sellerConfirmed;
    private long timestamp;
    private String paymentMethod;
    private int total;               // Tổng tiền gốc
    private double totalAmount;      // Tổng tiền sau giảm giá (offer)
    private double discount;         // Số tiền giảm giá (nếu có)
    private String orderType;        // "normal" hoặc "offer"
    private String imageUrl;         // Hình ảnh đầu tiên của đơn

    private List<ItemInOrder> items; // Danh sách sản phẩm trong đơn

    public OrderHistoryModel() {}

    // Getters & Setters cho toàn bộ field
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean isBuyerConfirmed() { return buyerConfirmed; }
    public void setBuyerConfirmed(boolean buyerConfirmed) { this.buyerConfirmed = buyerConfirmed; }
    public boolean isSellerConfirmed() { return sellerConfirmed; }
    public void setSellerConfirmed(boolean sellerConfirmed) { this.sellerConfirmed = sellerConfirmed; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    // Thêm getter/setter cho totalAmount
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    // Thêm getter/setter cho discount
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    // Thêm getter/setter cho orderType
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public List<ItemInOrder> getItems() { return items; }
    public void setItems(List<ItemInOrder> items) { this.items = items; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Nested class cho item trong đơn
    public static class ItemInOrder {
        private String productId;
        private String title;
        private int price;
        private int quantity;
        private String imageUrl;
        private String sellerId;

        public ItemInOrder() {}

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public int getPrice() { return price; }
        public void setPrice(int price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getSellerId() { return sellerId; }
        public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    }
}
