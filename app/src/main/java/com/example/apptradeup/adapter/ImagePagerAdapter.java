package com.example.apptradeup.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.apptradeup.R;
import com.example.apptradeup.FragmentAddListing.FragmentManageListings;


import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
    private final List<String> imageUris;
    private final Context context;
    private FragmentManageListings fragmentManageListings;

    public ImagePagerAdapter(Context context, List<String> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
        this.fragmentManageListings = null; // hoặc có thể bỏ luôn nếu không dùng
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_fullscreen, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String uri = imageUris.get(position);

        if (uri.startsWith("http")) {
            // Load từ Cloudinary / internet
            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.ic_add_photos)
                    .into(holder.imageView);
        } else {
            // Load ảnh cục bộ
            holder.imageView.setImageURI(Uri.parse(uri));
        }
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageFull);
        }
    }
}

