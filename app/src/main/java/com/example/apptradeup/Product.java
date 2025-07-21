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
        private String sellerId;
        private String Status;
        private List<String> images;
        private List<Review> reviews;
        private double averageRating; // trung bình
        private int ratingCount;      // số lượt đánh giá
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
                String sellerId,
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
            this.sellerId = sellerId;
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

        public String getSellerId() {
            return sellerId;
        }

        public void setSellerId(String sellerId) {
            this.sellerId = sellerId;
        }

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
        public static class Review implements java.io.Serializable {
            private String buyerId;
            private String buyerDisplayName;    // tên người mua
            private String buyerAvatarUrl;      // avatar người mua
            private String comment;
            private int rating;
            private long timestamp;

            public Review() {}

            public Review(String buyerId, String buyerDisplayName, String buyerAvatarUrl, String comment, int rating, long timestamp) {
                this.buyerId = buyerId;
                this.buyerDisplayName = buyerDisplayName;
                this.buyerAvatarUrl = buyerAvatarUrl;
                this.comment = comment;
                this.rating = rating;
                this.timestamp = timestamp;
            }

            public String getBuyerId() { return buyerId; }
            public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

            public String getBuyerDisplayName() { return buyerDisplayName; }
            public void setBuyerDisplayName(String buyerDisplayName) { this.buyerDisplayName = buyerDisplayName; }

            public String getBuyerAvatarUrl() { return buyerAvatarUrl; }
            public void setBuyerAvatarUrl(String buyerAvatarUrl) { this.buyerAvatarUrl = buyerAvatarUrl; }

            public String getComment() { return comment; }
            public void setComment(String comment) { this.comment = comment; }
            public int getRating() { return rating; }
            public void setRating(int rating) { this.rating = rating; }
            public long getTimestamp() { return timestamp; }
            public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        }
        public List<Review> getReviews() { return reviews; }
        public void setReviews(List<Review> reviews) { this.reviews = reviews; }
        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
        public int getRatingCount() { return ratingCount; }
        public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }


    }




