package com.example.apptradeup.FragmentHome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.MainActivity;
import com.example.apptradeup.adapter.ProductAdapter;
import com.example.apptradeup.Product;
import com.example.apptradeup.R;
import com.example.apptradeup.util.GridSpacingItemDecoration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FragmentSellerProfile extends Fragment {

    private ImageView imgAvatar, btnBack;
    private Button btnChat;
    private TextView txtName, txtEmail, txtRating, txtProductCount;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<Product> sellerProducts = new ArrayList<>();
    private String sellerId;
    private FirebaseFirestore db;

    public FragmentSellerProfile() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_profile, container, false);

        recyclerView = view.findViewById(R.id.recyclerSellerProducts);
        imgAvatar = view.findViewById(R.id.imgSellerAvatar);
        txtName = view.findViewById(R.id.txtSellerName);
        txtEmail = view.findViewById(R.id.txtSellerEmail);
        txtRating = view.findViewById(R.id.txtRating);
        txtProductCount = view.findViewById(R.id.txtProductCount);
        btnChat = view.findViewById(R.id.btnChat);
        btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openHomeFragment();
            }
        });

        Bundle args = getArguments();
        if (args == null || (sellerId = args.getString("sellerId")) == null) {
            Toast.makeText(getContext(), "Không tìm thấy người bán", Toast.LENGTH_SHORT).show();
            return view;
        }

        db = FirebaseFirestore.getInstance();

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 1, true));
        adapter = new ProductAdapter(getContext(), sellerProducts);
        recyclerView.setAdapter(adapter);

        loadSellerInfo(sellerId);
        loadSellerProducts(sellerId);

        return view;
    }

    private void loadSellerInfo(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("display_name");
                        String email = documentSnapshot.getString("email");
                        String avatarUrl = documentSnapshot.getString("profile_pic_url");
                        Double rating = documentSnapshot.getDouble("rating_avg");

                        txtName.setText(name != null ? name : "Không rõ");
                        txtEmail.setText(email != null ? email : "Không rõ");

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Picasso.get().load(avatarUrl).into(imgAvatar);
                        }
                        if (rating != null) {
                            txtRating.setText("\u2B50 " + String.format("%.1f", rating));
                        } else {
                            txtRating.setText("\u2B50 Chưa có đánh giá");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải thông tin người bán", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSellerProducts(String userId) {
        db.collection("items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sellerProducts.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        sellerProducts.add(product);
                    }
                    txtProductCount.setText("\u2022 " + sellerProducts.size() + " sản phẩm");
                    adapter.updateList(new ArrayList<>(sellerProducts));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                });
    }
}

