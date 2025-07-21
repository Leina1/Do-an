package com.example.apptradeup.FragmentAddListing;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.apptradeup.R;
import com.example.apptradeup.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
public class FragmentViewItem extends Fragment {
    private ViewPager2 viewPagerImages;
    private TextView tvTitle, tvCategory, tvCondition, tvQty, tvPrice, tvDescription, tvLocation;
    private Button btnPost, btnEdit;

    private String title, category, condition, description, location;
    private int quantity;
    private double price;
    private ArrayList<String> imageUris;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_previewitem, container, false);

        viewPagerImages = view.findViewById(R.id.viewPagerImages);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvCondition = view.findViewById(R.id.tvCondition);
        tvQty = view.findViewById(R.id.tvQty);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvLocation = view.findViewById(R.id.tvLocation);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnPost = view.findViewById(R.id.btnPost);

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString("title", "");
            category = args.getString("category", "");
            condition = args.getString("condition", "");
            quantity = args.getInt("quantity", 1);
            price = args.getDouble("price", 0);
            description = args.getString("description", "");
            location = args.getString("location", "");
            imageUris = args.getStringArrayList("imageUris");

            tvTitle.setText(title);
            tvCategory.setText("Category: " + category);
            tvCondition.setText("Condition: " + condition);
            tvQty.setText("Qty: " + quantity);
            tvPrice.setText("Price: " + String.format("%,.0f VNĐ", price));
            tvDescription.setText(description);
            tvLocation.setText(location);

            if (imageUris != null && !imageUris.isEmpty()) {
                ImagePagerAdapter adapter = new ImagePagerAdapter(requireContext(), imageUris);
                viewPagerImages.setAdapter(adapter);
                viewPagerImages.setClipToPadding(false);
                viewPagerImages.setClipChildren(false);
                viewPagerImages.setOffscreenPageLimit(3);
                viewPagerImages.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
        }
        String productId = args.getString("productId", null);

        if (productId != null) {
            btnPost.setText("Save Edit");
        } else {
            btnPost.setText("Post");
        }
        btnEdit.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnPost.setOnClickListener(v -> {
            if (imageUris == null || imageUris.isEmpty()) {
                Toast.makeText(getActivity(), "No images to upload", Toast.LENGTH_SHORT).show();
                return;
            }

            if (productId != null) {
                uploadImagesToCloudinary(imageUris, uploadedUrls -> updateProductInFirestore(productId, uploadedUrls));
            } else {
                uploadImagesToCloudinary(imageUris, this::saveProductToFirestore);
            }
        });

        return view;
    }
    public interface OnImagesUploadedCallback {
        void onComplete(List<String> uploadedUrls);
    }
    private void uploadImagesToCloudinaryAndSave(List<String> imageUris) {
        List<String> uploadedUrls = new ArrayList<>();

        for (String uriStr : imageUris) {
            try {
                Uri uri = Uri.parse(uriStr);
                File file = copyUriToTempFile(uri);

                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(),
                                RequestBody.create(file, MediaType.parse("image/*")))
                        .addFormDataPart("upload_preset", "MyShopApp")
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.cloudinary.com/v1_1/diumhctnb/image/upload")
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            try {
                                JSONObject jsonObject = new JSONObject(responseBody);
                                String imageUrl = jsonObject.getString("secure_url");
                                synchronized (uploadedUrls) {
                                    uploadedUrls.add(imageUrl);
                                    if (uploadedUrls.size() == imageUris.size()) {
                                        requireActivity().runOnUiThread(() ->
                                                saveProductToFirestore(uploadedUrls));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getActivity(), "Server upload failed", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProductToFirestore(List<String> imageUrls) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String sellerId  = user.getUid();

        Product product = new Product(
                title,
                description,
                price,
                quantity,
                location,
                condition,
                category,
                sellerId ,
                "Available",
                imageUrls
        );
        product.setSold(0);
        product.setReviews(new ArrayList<>()); // Khởi tạo danh sách review rỗng
        product.setAverageRating(0.0);         // Trung bình đánh giá = 0
        product.setRatingCount(0);             // Số lượt đánh giá = 0
        db.collection("items")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "Product posted successfully", Toast.LENGTH_SHORT).show();
                    FragmentManageListings fragment = new FragmentManageListings();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private File copyUriToTempFile(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new Exception("Cannot open stream from uri: " + uri);

        File tempFile = File.createTempFile("upload", ".jpg", requireContext().getCacheDir());
        tempFile.deleteOnExit();

        try (BufferedSink sink = Okio.buffer(Okio.sink(tempFile))) {
            sink.writeAll(Okio.source(inputStream));
        }
        inputStream.close();
        return tempFile;
    }

    public static class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
        private final List<String> images;
        private final Context context;

        public ImagePagerAdapter(Context context, List<String> images) {
            this.context = context;
            this.images = images;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_image_fullscreen, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String data = images.get(position);
            if (data.startsWith("http")) {
                // nếu đã là link
                Glide.with(context)
                        .load(data)
                        .placeholder(R.drawable.ic_add_photos)
                        .into(holder.imageView);
            } else {
                // content:// local uri
                holder.imageView.setImageURI(Uri.parse(data));
            }
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageFull);
            }
        }
    }
    private void uploadImagesToCloudinary(List<String> imageUris, OnImagesUploadedCallback callback) {
        List<String> uploadedUrls = new ArrayList<>();

        for (String uriStr : imageUris) {
            if (uriStr.startsWith("http")) {
                uploadedUrls.add(uriStr);
                if (uploadedUrls.size() == imageUris.size()) {
                    requireActivity().runOnUiThread(() -> callback.onComplete(uploadedUrls));
                }
            } else {
                try {
                    Uri uri = Uri.parse(uriStr);
                    File file = copyUriToTempFile(uri);

                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.getName(),
                                    RequestBody.create(file, MediaType.parse("image/*")))
                            .addFormDataPart("upload_preset", "MyShopApp")
                            .build();

                    Request request = new Request.Builder()
                            .url("https://api.cloudinary.com/v1_1/diumhctnb/image/upload")
                            .post(requestBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getActivity(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String responseBody = response.body().string();
                                try {
                                    JSONObject jsonObject = new JSONObject(responseBody);
                                    String imageUrl = jsonObject.getString("secure_url");
                                    synchronized (uploadedUrls) {
                                        uploadedUrls.add(imageUrl);
                                        if (uploadedUrls.size() == imageUris.size()) {
                                            requireActivity().runOnUiThread(() -> callback.onComplete(uploadedUrls));
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(getActivity(), "Server upload failed", Toast.LENGTH_SHORT).show());
                            }
                        }
                    });

                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateProductInFirestore(String productId, List<String> imageUrls) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("description", description);
        updates.put("price", price);
        updates.put("quantity", quantity);
        updates.put("location", location);
        updates.put("condition", condition);
        updates.put("category", category);
        updates.put("images", imageUrls);

        db.collection("items").document(productId)
                .update(
                        "title", title,
                        "description", description,
                        "price", price,
                        "quantity", quantity,
                        "location", location,
                        "condition", condition,
                        "category", category,
                        "images", imageUrls,
                        "status", quantity > 0 ? "Available" : "Sold"
                )

                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Product updated successfully", Toast.LENGTH_SHORT).show();
                    FragmentManageListings fragment = new FragmentManageListings();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Update error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
