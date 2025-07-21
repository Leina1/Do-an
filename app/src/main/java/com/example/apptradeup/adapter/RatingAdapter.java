package com.example.apptradeup.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.apptradeup.Product;
import com.example.apptradeup.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder> {

    private Context context;
    private List<Product> productList;
    private String userId; // User hiện tại

    public RatingAdapter(Context context, List<Product> productList, String userId) {
        this.context = context;
        this.productList = productList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rating, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        Product product = productList.get(position);

        // Hiện tên và ảnh sản phẩm
        holder.tvProductName.setText(product.getTitle());
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(context).load(product.getImages().get(0)).into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
        }

        // Kiểm tra user đã review chưa
        Product.Review myReview = null;
        if (product.getReviews() != null) {
            for (Product.Review review : product.getReviews()) {
                if (review.getBuyerId().equals(userId)) {
                    myReview = review;
                    break;
                }
            }
        }

        if (myReview != null) {
            // ĐÃ ĐÁNH GIÁ: Ẩn form, hiện comment và điểm
            holder.ratingBar.setRating(myReview.getRating());
            holder.ratingBar.setIsIndicator(true); // Không cho chỉnh sửa
            holder.edtReview.setVisibility(View.GONE);
            holder.btnSubmit.setVisibility(View.GONE);
            holder.tvRated.setVisibility(View.VISIBLE);
            holder.tvRated.setText("Bạn đã đánh giá: " + myReview.getRating() + " sao\n\"" + myReview.getComment() + "\"");
        } else {
            // CHƯA ĐÁNH GIÁ: Hiện form đánh giá
            holder.ratingBar.setRating(0);
            holder.ratingBar.setIsIndicator(false);
            holder.edtReview.setVisibility(View.VISIBLE);
            holder.btnSubmit.setVisibility(View.VISIBLE);
            holder.tvRated.setVisibility(View.GONE);

            holder.btnSubmit.setOnClickListener(v -> {
                int rating = (int) holder.ratingBar.getRating();
                String comment = holder.edtReview.getText().toString().trim();

                if (rating == 0) {
                    Toast.makeText(context, "Vui lòng chọn số sao!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(comment)) {
                    Toast.makeText(context, "Vui lòng nhập nhận xét!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String displayName = "";   // Lấy từ profile hoặc user collection nếu cần
                String avatarUrl = "";     // Lấy từ profile hoặc user collection nếu cần

                Product.Review review = new Product.Review(
                        userId,
                        displayName,   // truyền display name (nếu có)
                        avatarUrl,     // truyền avatar url (nếu có)
                        comment,
                        rating,
                        System.currentTimeMillis()
                );
                // Cập nhật vào Firestore
                FirebaseFirestore.getInstance()
                        .collection("items")
                        .document(product.getId())
                        .update("reviews", FieldValue.arrayUnion(review))
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                            // Cập nhật giao diện
                            product.getReviews().add(review);
                            notifyItemChanged(holder.getAdapterPosition());
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Lỗi khi gửi đánh giá", Toast.LENGTH_SHORT).show();
                        });
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class RatingViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvRated;
        RatingBar ratingBar;
        EditText edtReview;
        Button btnSubmit;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            edtReview = itemView.findViewById(R.id.edtReview);
            btnSubmit = itemView.findViewById(R.id.btnSubmit);
            tvRated = new TextView(itemView.getContext());
            // Thêm TextView này vào layout nếu chưa có trong XML, hoặc tìm bằng findViewById nếu đã có
            // Nếu XML có TextView với id tvRated, dùng:
            // tvRated = itemView.findViewById(R.id.tvRated);
        }
    }
}
