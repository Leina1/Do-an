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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private Context context;
    private List<Product.Review> reviewList;

    public ReviewAdapter(Context context, List<Product.Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product.Review review = reviewList.get(position);

        // Nếu có displayName/avatar thì hiển thị luôn
        if (!TextUtils.isEmpty(review.getBuyerDisplayName())) {
            holder.tvUser.setText(review.getBuyerDisplayName());
        } else {
            holder.tvUser.setText("Người mua");
            // Truy vấn Firestore lấy display_name nếu chưa có
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(review.getBuyerId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String name = documentSnapshot.getString("display_name");
                        if (!TextUtils.isEmpty(name)) {
                            holder.tvUser.setText(name);
                        }
                    });
        }

        // Nếu có avatar thì hiển thị, không thì lấy từ users
        if (!TextUtils.isEmpty(review.getBuyerAvatarUrl())) {
            Glide.with(context)
                    .load(review.getBuyerAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.imgAvatar);
        } else {
            // Nếu thiếu avatar thì truy vấn users collection
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(review.getBuyerId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String avatar = documentSnapshot.getString("profile_pic_url");
                        if (!TextUtils.isEmpty(avatar)) {
                            Glide.with(context)
                                    .load(avatar)
                                    .placeholder(R.drawable.ic_profile)
                                    .into(holder.imgAvatar);
                        } else {
                            holder.imgAvatar.setImageResource(R.drawable.ic_profile);
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.imgAvatar.setImageResource(R.drawable.ic_profile);
                    });
        }

        holder.ratingBar.setRating(review.getRating());
        holder.tvComment.setText(review.getComment());
        holder.tvDate.setText(formatTimestamp(review.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUser, tvComment, tvDate;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUser = itemView.findViewById(R.id.tvUser);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
}
