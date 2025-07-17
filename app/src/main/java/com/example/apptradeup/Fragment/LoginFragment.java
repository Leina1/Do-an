package com.example.apptradeup.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.apptradeup.MainActivity;
import com.example.apptradeup.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private TextView signUpLink;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, googleSignInButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo Firebase Auth và Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Khởi tạo ActivityResultLauncher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        handleGoogleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.getData()));
                    } else {
                        // Xử lý khi người dùng hủy đăng nhập
                        Log.d(TAG, "Người dùng đã hủy đăng nhập Google");
                        Toast.makeText(requireContext(), "Đăng nhập bị hủy", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Ánh xạ view
        signUpLink = view.findViewById(R.id.TvSignin);
        emailEditText = view.findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = view.findViewById(R.id.editTextTextPassword);
        loginButton = view.findViewById(R.id.btnLogin);
        googleSignInButton = view.findViewById(R.id.google_signIn);

        // Xử lý sự kiện
        setupEventListeners();

        return view;
    }

    private void setupEventListeners() {
        // Chuyển sang màn hình đăng ký
        signUpLink.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Đăng nhập bằng email/password
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                signInWithEmail(email, password);
            }
        });

        // Đăng nhập bằng Google
        googleSignInButton.setOnClickListener(v -> {
            signInWithGoogle();
        });
    }

    private void signInWithGoogle() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo Google Sign-In: " + e.getMessage());
            Toast.makeText(requireContext(), "Lỗi hệ thống", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "ID Token: " + account.getIdToken());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.e(TAG, "Mã lỗi: " + e.getStatusCode() + ", " + e.getMessage());
            switch (e.getStatusCode()) {
                case 10: // DEVELOPER_ERROR
                    Toast.makeText(requireContext(), "Lỗi cấu hình developer", Toast.LENGTH_SHORT).show();
                    break;
                case 12501: // SIGN_IN_CANCELLED
                    Toast.makeText(requireContext(), "Đăng nhập bị hủy", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(requireContext(), "Lỗi không xác định", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase authentication with Google successful");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkAndCreateUserInFirestore(user);
                        }
                    } else {
                        Log.w(TAG, "Firebase authentication with Google failed", task.getException());
                        Toast.makeText(requireContext(), "Xác thực Firebase thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkAndCreateUserInFirestore(FirebaseUser user) {
        String uid = user.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && !document.exists()) {
                    // Người dùng mới - tạo bản ghi trong Firestore
                    createNewUserDocument(userRef, user);
                } else {
                    // Người dùng đã tồn tại - chuyển đến MainActivity
                    navigateToMainActivity(uid);
                }
            } else {
                Log.w(TAG, "Kiểm tra người dùng trong Firestore thất bại", task.getException());
                Toast.makeText(requireContext(), "Lỗi kiểm tra thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewUserDocument(DocumentReference userRef, FirebaseUser user) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("display_name", user.getDisplayName());
        newUser.put("email", user.getEmail());
        newUser.put("profile_pic_url", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        newUser.put("bio", "");
        newUser.put("deleted_at", null);
        newUser.put("verified", true); // Google đã xác minh email
        newUser.put("birthday", "");
        newUser.put("phone", "");
        newUser.put("address", "");
        newUser.put("gender", "");
        newUser.put("rating_avg", 0);
        newUser.put("created_at", System.currentTimeMillis());
        newUser.put("last_active", System.currentTimeMillis());
        newUser.put("Cart", new ArrayList<String>());

        userRef.set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Tạo người dùng mới trong Firestore thành công");
                    navigateToMainActivity(user.getUid());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Lỗi khi tạo người dùng mới trong Firestore", e);
                    Toast.makeText(requireContext(), "Lỗi lưu thông tin người dùng", Toast.LENGTH_SHORT).show();
                });
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            navigateToMainActivity(user.getUid());
                        }
                    } else {
                        Toast.makeText(requireActivity(), "Đăng nhập thất bại: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMainActivity(String userId) {
        Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(requireActivity(), MainActivity.class)
                .putExtra("userId", userId));
        requireActivity().finish();
    }
}