package com.example.apptradeup.Fragment;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.apptradeup.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
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

import javax.annotation.Nullable;

import android.util.Log;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import java.util.List;
public class EditProfileFragment extends Fragment {
    private EditText edtName, edtBio, edtBirthday, edtEmail, edtPhone, edtAddress;
    private RadioGroup rgGender;
    private Button btnSave;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView profileImageView;
    private Uri imageUri;
    private Button btnGetLocation;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri); // Display image temporarily
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
    private FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(getActivity(), "Permission denied to access location", Toast.LENGTH_SHORT).show();
                }
            });
    //    private final ActivityResultLauncher<Intent> autocompleteLauncher =
//            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
//                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
//                    edtAddress.setText(place.getAddress());
//                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
//                    com.google.android.gms.common.api.Status status = Autocomplete.getStatusFromIntent(result.getData());
//                    Toast.makeText(getActivity(), "Lỗi tìm kiếm địa điểm: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        profileImageView = view.findViewById(R.id.profile_image);

        edtName = view.findViewById(R.id.edtName);
        edtBio = view.findViewById(R.id.edtBio);
        edtBirthday = view.findViewById(R.id.edtBirthday);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtAddress = view.findViewById(R.id.edtAddress);
        rgGender = view.findViewById(R.id.rgGender);
        btnSave = view.findViewById(R.id.btnSave);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        // Request location permission if needed

//        if (!Places.isInitialized()) {
//            Places.initialize(requireContext().getApplicationContext(), getString(R.string.google_maps_key), Locale.getDefault());
//        }


        // Event listener for image click to pick a new image
        profileImageView.setOnClickListener(v -> checkStoragePermission());

        edtAddress.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });


        // Fetch user data from Firestore and populate the fields
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference docRef = db.collection("users").document(userId);

            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    edtName.setText(documentSnapshot.getString("display_name"));
                    edtBio.setText(documentSnapshot.getString("bio"));
                    edtBirthday.setText(documentSnapshot.getString("birthday"));
                    edtEmail.setText(documentSnapshot.getString("email"));
                    edtPhone.setText(documentSnapshot.getString("phone"));
                    edtAddress.setText(documentSnapshot.getString("address"));
                    String gender = documentSnapshot.getString("gender");
                    if (gender != null) {
                        if (gender.equalsIgnoreCase("Nam")) {
                            rgGender.check(R.id.rbMale);
                        } else if (gender.equalsIgnoreCase("Nữ")) {
                            rgGender.check(R.id.rbFemale);
                        }
                    }
                    // Load profile image from URL using Picasso
                    String profilePicUrl = documentSnapshot.getString("profile_pic_url");
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Picasso.get().load(profilePicUrl).into(profileImageView);
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        // Save updated user data
        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String bio = edtBio.getText().toString();
            String birthday = edtBirthday.getText().toString();
            String email = edtEmail.getText().toString();
            String phone = edtPhone.getText().toString();
            String address = edtAddress.getText().toString();
            int selectedId = rgGender.getCheckedRadioButtonId();
            String gender = "";

            if (selectedId == R.id.rbMale) gender = "Nam";
            else if (selectedId == R.id.rbFemale) gender = "Nữ";

            if (currentUser != null) {
                String userId = currentUser.getUid();
                if (imageUri != null) {
                    uploadImageToCloudinary(imageUri, userId, name, bio, birthday, email, phone, address,gender);
                } else {
                    updateUserProfile(userId, name, bio, birthday, email, phone, address,gender, null);
                }
            }
        });
        return view;
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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageToCloudinary(Uri uri, String userId, String name, String bio, String birthday,
                                         String email, String phone, String address, String gender) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload", ".jpg", requireContext().getCacheDir());
            BufferedSink sink = Okio.buffer(Okio.sink(tempFile));
            sink.writeAll(Okio.source(inputStream));
            sink.close();

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", tempFile.getName(), RequestBody.create(tempFile, MediaType.parse("image/*")))
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
                            Toast.makeText(getActivity(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (!response.isSuccessful()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getActivity(), "Upload failed with code: " + response.code(), Toast.LENGTH_SHORT).show()
                            );
                            return;
                        }

                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String imageUrl = jsonObject.getString("secure_url");

                        requireActivity().runOnUiThread(() -> {
                            updateUserProfile(userId, name, bio, birthday, email, phone, address, gender, imageUrl);
                            Toast.makeText(getActivity(), "Uploaded to Cloudinary", Toast.LENGTH_SHORT).show();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error reading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserProfile(String userId, String name, String bio, String birthday,
                                   String email, String phone, String address, String gender, String profilePicUrl) {
        DocumentReference docRef = db.collection("users").document(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("display_name", name);
        data.put("bio", bio);
        data.put("birthday", birthday);
        data.put("email", email);
        data.put("phone", phone);
        data.put("address", address);
        data.put("gender", gender);
        if (profilePicUrl != null) data.put("profile_pic_url", profilePicUrl);

        docRef.update(data)
                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show());
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

}
