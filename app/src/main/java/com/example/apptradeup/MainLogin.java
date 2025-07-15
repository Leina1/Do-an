package com.example.apptradeup;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.apptradeup.Fragment.LoginFragment;

public class MainLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainlogin);

        // Hiển thị LoginFragment mặc định
        if (savedInstanceState == null) {
            Fragment fragment = new LoginFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment); // Đảm bảo có FrameLayout với id="fragment_container" trong layout
            transaction.commit();
        }
    }
}