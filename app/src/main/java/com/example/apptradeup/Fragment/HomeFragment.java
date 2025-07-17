package com.example.apptradeup.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.text.Editable;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.adapter.ProductAdapter;
import com.example.apptradeup.Product;
import com.example.apptradeup.R;
import com.example.apptradeup.CartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.apptradeup.util.GridSpacingItemDecoration;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private final List<Product> fullList = new ArrayList<>();
    private FirebaseFirestore db;
    private GridLayoutManager layoutManager;
    private EditText searchEditText;
    private ImageButton btnCart;
    private String userId;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        searchEditText = view.findViewById(R.id.searchEditText);
        btnCart = view.findViewById(R.id.btnCart);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Khởi tạo layoutManager
        layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager); // Sử dụng biến đã khai báo

        // Phần còn lại giữ nguyên
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 1, true));

        adapter = new ProductAdapter(getContext(), productList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadProductsFromFirestore();
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        btnCart.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), CartActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        loadProductsFromFirestore();  // Mỗi lần quay lại màn hình home sẽ reload sản phẩm
    }
    private void loadProductsFromFirestore() {
        db.collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    fullList.clear(); // <-- clear luôn danh sách gốc

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        if ("Available".equalsIgnoreCase(product.getStatus())) {
                            productList.add(product);
                            fullList.add(product); // <-- giữ dữ liệu gốc để lọc
                        }
                    }
                    adapter.updateList(new ArrayList<>(productList)); // <-- cập nhật adapter
                })
                .addOnFailureListener(e -> {
                    // xử lý lỗi nếu cần
                });
    }
    private void filterProducts(String query) {
        if (query.isEmpty()) {
            adapter.updateList(new ArrayList<>(fullList)); // <-- dùng danh sách gốc đầy đủ
            return;
        }

        List<Product> filteredList = new ArrayList<>();
        for (Product product : fullList) {
            if (product.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    product.getCategory().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        adapter.updateList(filteredList);
    }


}
