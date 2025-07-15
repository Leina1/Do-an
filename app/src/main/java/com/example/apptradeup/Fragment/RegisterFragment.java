package com.example.apptradeup.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.example.apptradeup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterFragment extends Fragment {
    private TextView SignInLink;
    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        SignInLink = view.findViewById(R.id.TvsignInLink);
        fullNameEditText = view.findViewById(R.id.fullNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        registerButton = view.findViewById(R.id.BtnregisterButton);
        mAuth = FirebaseAuth.getInstance();
        SignInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        registerButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else if (!isPasswordValid(password)) {
                Toast.makeText(getActivity(), "Password must be at least 6 characters long and contain a special character", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(fullName, email, password);
            }
        });
        return view;
    }
    public boolean isPasswordValid(String password) {
        // Kiểm tra độ dài mật khẩu
        if (password.length() < 6) {
            return false;  // Mật khẩu phải có ít nhất 6 ký tự
        }

        // Kiểm tra có ít nhất một ký tự đặc biệt
        String specialCharacters = "(.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*)";
        if (!password.matches(specialCharacters)) {
            return false;  // Mật khẩu phải có ít nhất một ký tự đặc biệt
        }

        return true;  // Nếu tất cả điều kiện hợp lệ
    }

    private void registerUser(String fullName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Lưu thông tin người dùng vào Firestore
                            saveUserToFirestore(user, fullName, email);
                        }
                    } else {
                        // Đăng ký thất bại
                        Toast.makeText(getActivity(), "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveUserToFirestore(FirebaseUser user, String fullName, String email){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("bio","");
        userMap.put("Deleted_at",null);
        userMap.put("display_name", fullName);
        userMap.put("email", email);
        userMap.put("created_at", System.currentTimeMillis());  // Thêm thời gian tạo tài khoản
        userMap.put("last_active", System.currentTimeMillis());  // Thêm thời gian hoạt động gần nhất
        userMap.put("rating_avg", 0);  // Khởi tạo điểm đánh giá người dùng
        userMap.put("profile_pic_url", "");  // Khởi tạo ảnh đại diện (có thể thêm sau)
        userMap.put("Address", "");  // Khởi tạo địa chỉ (có thể thêm sau)
        userMap.put("verified", false); // Đánh dấu người dùng chưa xác minh (sẽ thay đổi sau khi xác minh email)
        userMap.put("phone", "");
        userMap.put("Gender", "");
        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Lưu thành công
                    Toast.makeText(getActivity(), "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    // Gửi email xác minh
                    sendVerificationEmail(user);
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi lưu dữ liệu
                    Toast.makeText(getActivity(), "Lỗi lưu dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Gửi email xác minh thành công
                        Toast.makeText(getActivity(), "Đăng ký thành công! Vui lòng kiểm tra email để xác minh tài khoản.", Toast.LENGTH_SHORT).show();
                        // Quay lại trang LoginFragment
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new LoginFragment())
                                .addToBackStack(null)
                                .commit();
                    } else {
                        // Xử lý lỗi khi gửi email xác minh
                        Toast.makeText(getActivity(), "Lỗi gửi email xác minh: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
