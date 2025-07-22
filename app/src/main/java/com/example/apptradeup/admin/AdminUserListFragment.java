package com.example.apptradeup.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.apptradeup.R;
import com.example.apptradeup.User;
import com.google.firebase.firestore.*;

import java.util.*;

public class AdminUserListFragment extends Fragment {
    private RecyclerView recyclerView;
    private EditText edtSearchUserId;
    private UserAdminAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private List<User> fullUserList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_user_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerUsers);
        edtSearchUserId = view.findViewById(R.id.edtSearchUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdminAdapter(userList, userActionListener);
        recyclerView.setAdapter(userAdapter);

        loadUsers();

        edtSearchUserId.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUser(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    fullUserList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            fullUserList.add(user);
                        }
                    }
                    userList.clear();
                    userList.addAll(fullUserList);
                    userAdapter.notifyDataSetChanged();
                });
    }

    private void filterUser(String query) {
        List<User> filtered = new ArrayList<>();
        for (User user : fullUserList) {
            if (user.getId() != null && user.getId().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(user);
            }
        }
        userList.clear();
        userList.addAll(filtered);
        userAdapter.notifyDataSetChanged();
    }

    // Adapter callback xử lý ban/delete
    private final UserAdminAdapter.OnUserActionListener userActionListener = new UserAdminAdapter.OnUserActionListener() {
        @Override
        public void onBanClicked(User user) {
            boolean newBannedStatus = !user.isBanned();
            String newStatus = newBannedStatus ? "banned" : "active";

            Map<String, Object> updates = new HashMap<>();
            updates.put("banned", newBannedStatus);
            updates.put("status", newStatus);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getId())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        user.setBanned(newBannedStatus);
                        user.setStatus(newStatus);
                        userAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(),
                                (newBannedStatus ? "Đã cấm" : "Đã mở cấm") + " user",
                                Toast.LENGTH_SHORT).show();
                    });
        }

        @Override
        public void onDeleteClicked(User user) {
            // Xác nhận xoá
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Xoá tài khoản")
                    .setMessage("Bạn chắc chắn muốn xoá tài khoản này?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        FirebaseFirestore.getInstance().collection("users").document(user.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    userList.remove(user);
                                    fullUserList.remove(user);
                                    userAdapter.notifyDataSetChanged();
                                    Toast.makeText(getContext(), "Đã xoá user", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        }
    };
}
