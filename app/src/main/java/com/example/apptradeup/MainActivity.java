package com.example.apptradeup;

import android.os.Bundle;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.apptradeup.Fragment.HomeFragment;
import com.example.apptradeup.Fragment.EditProfileFragment;
import com.example.apptradeup.FragmentAddListing.FragmentAddItem;
import com.example.apptradeup.FragmentAddListing.FragmentManageListings;
import com.example.apptradeup.FragmentHome.FragmentSellerProfile;




public class MainActivity extends AppCompatActivity {
    private String userId;
    private HomeFragment homeFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ✅ Gọi 1 lần duy nhất

        // Lấy userId từ Intent
        userId = getIntent().getStringExtra("userId");

        // Gán nút
        Button btnHome = findViewById(R.id.btbHome);
        Button btnAddListing = findViewById(R.id.btnAdd_Listing);
        Button btnNotifications = findViewById(R.id.btnNotifications);
        Button btnProfile = findViewById(R.id.btnProfile);

        // Kiểm tra intent nếu mở từ avatar → mở profile người bán
        if (getIntent() != null && getIntent().getBooleanExtra("openSellerProfile", false)) {
            String sellerId = getIntent().getStringExtra("sellerId");
            if (sellerId != null) {
                openSellerProfile(sellerId);
                return; // ⛔ Dừng để tránh load HomeFragment ngay sau đó
            }
        }

        // Gán HomeFragment làm mặc định
        homeFragment = new HomeFragment();
        loadFragmentWithUserId(homeFragment);

        // Xử lý các nút bấm
        btnHome.setOnClickListener(v -> loadFragmentWithUserId(new HomeFragment()));
        btnProfile.setOnClickListener(v -> loadFragmentWithUserId(new EditProfileFragment()));
        btnAddListing.setOnClickListener(v -> loadFragmentWithUserId(new FragmentManageListings()));
    }
    /**
     * Hàm tải Fragment và truyền userId nếu cần
     */
    private void loadFragmentWithUserId(Fragment fragment) {
        if (userId != null) {
            Bundle args = new Bundle();
            args.putString("userId", userId)
            ;
            fragment.setArguments(args);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Cho phép quay lại
        transaction.commit();
    }
    public void openSellerProfile(String sellerId) {
        FragmentSellerProfile fragment = new FragmentSellerProfile();
        Bundle bundle = new Bundle();
        bundle.putString("sellerId", sellerId);
        fragment.setArguments(bundle);

        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e("FragmentError", "Error replacing fragment", e);
        }
    }
    public void openHomeFragment() {
        loadFragmentWithUserId(new HomeFragment());
    }

}