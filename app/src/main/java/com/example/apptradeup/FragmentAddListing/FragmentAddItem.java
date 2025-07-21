package com.example.apptradeup.FragmentAddListing;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.apptradeup.Product;
import com.example.apptradeup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import okio.BufferedSink;
import okio.Okio;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nullable;

import okio.BufferedSink;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

public class FragmentAddItem extends Fragment {
    private EditText edtAddress;
    private String sellerId ;
    private ImageView ItemImageView;
    private Uri imageUri;
    private List<Uri> imageUris = new ArrayList<>();
    private FrameLayout imageOverlayContainer;
    private LinearLayout carouselContainer;
    private Product editingProduct = null;
    private ImageButton btnGetLocation;
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        int totalSelected = data.getClipData().getItemCount();
                        int availableSlots = 10 - imageUris.size();
                        int toAdd = Math.min(totalSelected, availableSlots);
                        for (int i = 0; i < toAdd; i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            imageUris.add(uri);
                        }
                    } else if (data.getData() != null) {
                        if (imageUris.size() < 10) {
                            imageUris.add(data.getData());
                        } else {
                            Toast.makeText(getContext(), "Bạn đã chọn đủ 10 ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }

                    View view = imageOverlayContainer.getChildAt(0);
                    if (view != null) {
                        LinearLayout container = view.findViewById(R.id.layoutImageContainer);
                        HorizontalScrollView scrollView = view.findViewById(R.id.horizontalScrollView);
                        displayImages(container, scrollView);
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(getActivity(), "Permission denied to access storage", Toast.LENGTH_SHORT).show();
                }
            });
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
    private FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(getActivity(), "Permission denied to access location", Toast.LENGTH_SHORT).show();
                }
            });
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_additem, container, false);
        EditText editTextTitle = view.findViewById(R.id.editTextTitle);
        EditText editTextDescription = view.findViewById(R.id.editTextDescription);
        EditText editTextPrice = view.findViewById(R.id.editTextPrice);
        EditText editTextCategory = view.findViewById(R.id.editTextCategory);
        edtAddress = view.findViewById(R.id.editTextLocation);
        Button buttonAddItem = view.findViewById(R.id.btnPost);
        Button buttonPreview = view.findViewById(R.id.btnPreview);
        ItemImageView = view.findViewById(R.id.imageViewAddPhotos);
        imageOverlayContainer = view.findViewById(R.id.imageOverlayContainer);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        btnGetLocation = view.findViewById(R.id.btnGetAddress);
        ItemImageView.setOnClickListener(v -> {
            View carouselView = LayoutInflater.from(getContext()).inflate(R.layout.image_carousel, imageOverlayContainer, false);
            carouselContainer = carouselView.findViewById(R.id.layoutImageContainer);
            HorizontalScrollView scrollView = carouselView.findViewById(R.id.horizontalScrollView);  // <-- Sửa tại đây
            displayImages(carouselContainer, scrollView); // truyền scrollView vào
            imageOverlayContainer.addView(carouselView);
            imageOverlayContainer.bringToFront();
            imageOverlayContainer.setVisibility(View.VISIBLE);
        });
        imageOverlayContainer.setOnClickListener(v1 -> {
            imageOverlayContainer.setVisibility(View.GONE);
        });


        NumberPicker qtyPicker = view.findViewById(R.id.numberPickerQty);
        qtyPicker.setMinValue(1);
        qtyPicker.setMaxValue(1000);
        qtyPicker.setValue(1);

        Spinner spinnerCondition = view.findViewById(R.id.spinnerCondition);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.condition_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(adapter);
        if (getArguments() != null) {
            sellerId  = getArguments().getString("userId");
        }
        if (getArguments() != null && getArguments().getSerializable("product") != null) {
            editingProduct = (Product) getArguments().getSerializable("product");
            if (editingProduct != null) {
                editTextTitle.setText(editingProduct.getTitle());
                editTextDescription.setText(editingProduct.getDescription());
                editTextPrice.setText(String.valueOf(editingProduct.getPrice()));
                editTextCategory.setText(editingProduct.getCategory());
                edtAddress.setText(editingProduct.getLocation());
                qtyPicker.setValue(editingProduct.getQuantity());

                int pos = adapter.getPosition(editingProduct.getCondition());
                spinnerCondition.setSelection(pos);

                // load images
                if (editingProduct.getImages() != null) {
                    for (String url : editingProduct.getImages()) {
                        imageUris.add(Uri.parse(url));  // reuse the same list
                    }
                }

                buttonAddItem.setText("Save Edit");
            }
        }
        getParentFragmentManager().setFragmentResultListener("location_result", this, (requestKey, bundle) -> {
            String address = bundle.getString("address");
            double lat = bundle.getDouble("lat", 0.0);
            double lng = bundle.getDouble("lng", 0.0);
            Log.d("FragmentAddItem", "Đã nhận địa chỉ: " + address + " | lat: " + lat + ", lng: " + lng);

            if (address != null && !address.isEmpty()) {
                edtAddress.setText(address);
                // Lưu lại lat/lng vừa lấy vào biến instance
                this.selectedLat = lat;
                this.selectedLng = lng;
            }
        });

        edtAddress.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new com.example.apptradeup.Fragment.PickLocationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnGetLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
        buttonAddItem.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String priceStr = editTextPrice.getText().toString().trim();
            String category = editTextCategory.getText().toString().trim();
            String location = edtAddress.getText().toString().trim();
            int quantity = qtyPicker.getValue();
            String condition = spinnerCondition.getSelectedItem().toString();
            if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty() || category.isEmpty() || location.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                double price = Double.parseDouble(priceStr);
                if (editingProduct == null) {
                    // Thêm mới
                    uploadImagesToCloudinaryAndSaveOrUpdate(imageUris, title, description, price, quantity, location, condition, category, false, null, null);
                } else {
                    // Chỉnh sửa
                    uploadImagesToCloudinaryAndSaveOrUpdate(imageUris, title, description, price, quantity, location, condition, category, true, editingProduct.getId(), editingProduct);
                }

            }
        });

        buttonPreview.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String category = editTextCategory.getText().toString().trim();
            String location = edtAddress.getText().toString().trim();
            String priceStr = editTextPrice.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty() || category.isEmpty() || location.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = 0.0;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Invalid price", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUris == null || imageUris.isEmpty()) {
                Toast.makeText(getActivity(), "Please select at least one image", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            bundle.putString("description", description);
            bundle.putString("category", category);
            bundle.putString("location", location);
            bundle.putString("condition", spinnerCondition.getSelectedItem().toString());
            bundle.putDouble("price", price);
            bundle.putInt("quantity", qtyPicker.getValue());
            bundle.putDouble("lat", selectedLat);
            bundle.putDouble("lng", selectedLng);


            ArrayList<String> uriStrings = new ArrayList<>();
            for (Uri uri : new ArrayList<>(imageUris)) {  // clone lại list trước khi lặp
                uriStrings.add(uri.toString());
            }
            bundle.putStringArrayList("imageUris", uriStrings);
//            for (Uri uri : imageUris) {
//                uriStrings.add(uri.toString());
//            }
//            bundle.putStringArrayList("imageUris", uriStrings);
            if (editingProduct != null) {
                bundle.putString("productId", editingProduct.getId());
            }
            FragmentViewItem fragment = new FragmentViewItem();
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void saveProductToFirestore(
            String title,
            String description,
            double price,
            int quantity,
            String location,
            String condition,
            String category,
            List<String> imageUrls
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String sellerId  = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Product product = new Product(
                title,
                description,
                price,
                quantity,
                location,
                condition,
                category,  // thêm category
                sellerId,
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
                    String productId = documentReference.getId();
                    product.setId(productId);
                    Toast.makeText(getActivity(), "Product posted successfully", Toast.LENGTH_SHORT).show();
                    FragmentManageListings fragment = new FragmentManageListings();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Error posting product: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private File copyUriToTempFile(Uri uri) throws IOException {
        ContentResolver resolver = requireContext().getContentResolver();
        InputStream inputStream = resolver.openInputStream(uri);
        if (inputStream == null) throw new IOException("Cannot open stream from uri: " + uri);

        File tempFile = File.createTempFile("upload", ".jpg", requireContext().getCacheDir());
        tempFile.deleteOnExit();

        try (BufferedSink sink = Okio.buffer(Okio.sink(tempFile))) {
            sink.writeAll(Okio.source(inputStream));
        }
        inputStream.close();
        return tempFile;
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                openImagePicker();
            }
        } else { // API < 33
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openImagePicker();
            }
        }
    }
    private void displayImages(LinearLayout container, HorizontalScrollView scrollView) {
        container.removeAllViews();
        for (int i = 0; i < imageUris.size(); i++) {
            Uri uri = imageUris.get(i);
            FrameLayout frame = new FrameLayout(getContext());
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(240, 240);
            frameParams.setMarginEnd(16);
            frame.setLayoutParams(frameParams);

            ImageView imageView = new ImageView(getContext());
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(240, 240);
            imageView.setLayoutParams(imageParams);
            if (uri.toString().startsWith("http")) {
                // Là ảnh từ Cloudinary URL
                Picasso.get().load(uri.toString()).into(imageView);
            } else {
                // Là ảnh local từ máy (chọn từ gallery)
                imageView.setImageURI(uri);
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            ImageView closeBtn = new ImageView(getContext());
            FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(60, 60);
            closeParams.gravity = Gravity.TOP | Gravity.END;
            closeBtn.setLayoutParams(closeParams);
            closeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            closeBtn.setBackgroundColor(0x88FFFFFF);
            closeBtn.setOnClickListener(v -> {
                imageUris.remove(uri);
                displayImages(container, scrollView);
            });

            frame.addView(imageView);
            frame.addView(closeBtn);
            container.addView(frame);
        }
        FrameLayout btnAdd = new FrameLayout(getContext());
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(240, 240);
        addParams.setMargins(0, 0, 16, 0);
        btnAdd.setLayoutParams(addParams);
        btnAdd.setBackgroundResource(R.drawable.bg_image_placeholder);

        ImageView icon = new ImageView(getContext());
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(100, 100, Gravity.CENTER);
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.ic_add_photos);
        btnAdd.addView(icon);

        btnAdd.setOnClickListener(v -> checkStoragePermission());
        container.addView(btnAdd);

        scrollView.post(() -> scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT));
    }
    private void uploadImagesToCloudinaryAndSaveOrUpdate(
            List<Uri> imageUris,
            String title,
            String description,
            double price,
            int quantity,
            String location,
            String condition,
            String category,
            boolean isEdit,
            @Nullable String productId,
            @Nullable Product oldProduct
    ) {
        if (imageUris == null || imageUris.isEmpty()) {
            Toast.makeText(getActivity(), "Please select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> uploadedUrls = new ArrayList<>();

        for (Uri uri : imageUris) {
            if (uri.toString().startsWith("http")) {
                uploadedUrls.add(uri.toString());
                if (uploadedUrls.size() == imageUris.size()) {
                    if (isEdit) {
                        updateProductInFirestore(productId, title, description, price, quantity, location, condition, category, uploadedUrls, oldProduct);
                    } else {
                        saveProductToFirestore(title, description, price, quantity, location, condition, category, uploadedUrls);
                    }
                }
            } else {
                try {
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
                                            requireActivity().runOnUiThread(() -> {
                                                if (isEdit) {
                                                    updateProductInFirestore(productId, title, description, price, quantity, location, condition, category, uploadedUrls, oldProduct);
                                                } else {
                                                    saveProductToFirestore(title, description, price, quantity, location, condition, category, uploadedUrls);
                                                }
                                            });
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(getActivity(), "Upload failed: server error", Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateProductInFirestore(
            String productId,
            String title,
            String description,
            double price,
            int quantity,
            String location,
            String condition,
            String category,
            List<String> imageUrls,
            Product oldProduct
    ) {
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

        if (oldProduct != null && oldProduct.getStatus().equals("Sold") && quantity > 0) {
            updates.put("status", "Available");
        }

        db.collection("items").document(productId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Product updated successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new FragmentManageListings())
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Error updating product: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền thì không làm gì cả, hoặc hiện thông báo
            Toast.makeText(getActivity(), "Chưa được cấp quyền vị trí", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Hiển thị log (debug)
                        Log.d("Location", "Lat: " + latitude + ", Lng: " + longitude);

                        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String address = addresses.get(0).getAddressLine(0);
                                edtAddress.setText(address); // Gán địa chỉ vào EditText
                            } else {
                                edtAddress.setText("Lat: " + latitude + ", Lng: " + longitude);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            edtAddress.setText("Lat: " + latitude + ", Lng: " + longitude);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Không lấy được vị trí (null)", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public void onResume() {
        super.onResume();

        // Xử lý tránh trùng ảnh khi quay lại từ Preview
        if (imageUris != null) {
            Set<Uri> uniqueUris = new LinkedHashSet<>(imageUris);  // Giữ thứ tự, loại bỏ trùng
            imageUris.clear();
            imageUris.addAll(uniqueUris);
        }
    }

}
