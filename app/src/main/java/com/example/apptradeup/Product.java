package com.example.apptradeup;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class Product implements java.io.Serializable {
    private String id;
    private String title;
    private String description;
    private double price;
    private int quantity;
    private String location;
    private String condition;
    private String category; // thêm đầy đủ
    private String userId;
    private String Status;
    private List<String> images;
    private com.google.firebase.Timestamp timestamp;

    private int Sold = 0; // Biến này có thể dùng để theo dõi số lượng đã bán

    // Empty constructor (Firestore bắt buộc)
    public Product() {
    }

    // Constructor có category
    public Product(
            String title,
            String description,
            double price,
            int quantity,
            String location,
            String condition,
            String category,  // thêm category
            String userId,
            String status,
            List<String> images
    ) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.location = location;
        this.condition = condition;
        this.category = category;
        this.userId = userId;
        this.Status = status;
        this.images = images;
    }

    // Getter + Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStatus() { return Status; }
    public void setStatus(String Status) { this.Status = Status; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public com.google.firebase.Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(com.google.firebase.Timestamp timestamp) { this.timestamp = timestamp; }
    public int getSold() {
        return Sold;
    }

    public void setSold(int sold) {
        this.Sold = sold;
    }



}




