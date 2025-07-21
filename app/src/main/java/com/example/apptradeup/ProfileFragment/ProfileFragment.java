package com.example.apptradeup.ProfileFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.apptradeup.Fragment.LoginFragment;
import com.bumptech.glide.Glide;
import com.example.apptradeup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private String userId;
    private ImageView imgAvatar;
    private TextView tvDisplayName, tvEmail;
    private Button btnEditProfile, btnLogout;
    private LinearLayout layoutOrderHistory, layoutSellHistory, layoutReviews;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Lấy userId từ Bundle (nếu có)
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        // Ánh xạ view
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvDisplayName = view.findViewById(R.id.tvDisplayName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        layoutOrderHistory = view.findViewById(R.id.layoutOrderHistory);
        layoutSellHistory = view.findViewById(R.id.layoutSellHistory);
        layoutReviews = view.findViewById(R.id.layoutReviews);

        db = FirebaseFirestore.getInstance();

        // Lấy dữ liệu người dùng từ Firestore
        loadUserInfo();

        // Các sự kiện nút, giữ nguyên như trước
        btnEditProfile.setOnClickListener(v -> {
            // Tạo EditProfileFragment mới
            EditProfileFragment editProfileFragment = new EditProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            editProfileFragment.setArguments(bundle);

            // Thay fragment hiện tại bằng EditProfileFragment, lưu vào backstack
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, editProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        layoutOrderHistory.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new OrderHistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        layoutSellHistory.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SellHistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        layoutReviews.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RatingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getActivity(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

            // Xoá hết backstack
            requireActivity().getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);

            // Thay bằng LoginFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });

        return view;
    }

    private void loadUserInfo() {
        if (userId == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy userId", Toast.LENGTH_SHORT).show();
            return;
        }
        // Đường dẫn: collection "users" (hoặc "User", tuỳ Firestore của bạn), document userId
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String displayName = documentSnapshot.getString("display_name");
                String email = documentSnapshot.getString("email");
                String avatarUrl = documentSnapshot.getString("profile_pic_url");

                // Có thể lấy thêm các trường khác nếu muốn: phone, address, bio, ...
                tvDisplayName.setText(displayName != null ? displayName : "(Chưa có tên)");
                tvEmail.setText(email != null ? email : "(Chưa có email)");

                // Load avatar (nếu có link), dùng Glide hoặc setImageResource nếu mặc định
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_launcher_background) // Ảnh mặc định nếu chưa có
                            .error(R.drawable.ic_launcher_background)
                            .into(imgAvatar);
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_launcher_background); // Ảnh mặc định
                }
            } else {
                Toast.makeText(getContext(), "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
