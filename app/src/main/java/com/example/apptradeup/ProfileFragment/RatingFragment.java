package com.example.apptradeup.ProfileFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.Product;
import com.example.apptradeup.OrderHistoryModel;
import com.example.apptradeup.R;
import com.example.apptradeup.adapter.RatingAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class RatingFragment extends Fragment {

    private RecyclerView recyclerView;
    private RatingAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating, container, false);
        recyclerView = view.findViewById(R.id.recyclerRating);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo adapter với list rỗng, set luôn!
        adapter = new RatingAdapter(getContext(), productList, userId);
        recyclerView.setAdapter(adapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadCompletedBoughtProducts();

        return view;
    }

    private void loadCompletedBoughtProducts() {
        FirebaseFirestore.getInstance()
                .collection("orders")
                .whereEqualTo("buyerId", userId)
                .whereEqualTo("completed", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> productIds = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderHistoryModel order = doc.toObject(OrderHistoryModel.class);
                        if (order != null && order.getItems() != null) {
                            for (OrderHistoryModel.ItemInOrder item : order.getItems()) {
                                productIds.add(item.getProductId());
                            }
                        }
                    }
                    if (!productIds.isEmpty()) {
                        fetchProductDetails(new ArrayList<>(productIds));
                    } else {
                        adapter = new RatingAdapter(getContext(), new ArrayList<>(), userId);
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi khi lấy đơn hàng đã mua", Toast.LENGTH_SHORT).show());
    }

    // Lấy chi tiết sản phẩm và lọc những cái đã đánh giá ra ngoài
    private void fetchProductDetails(List<String> productIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        productList.clear();
        if (productIds.isEmpty()) {
            adapter.notifyDataSetChanged(); // Danh sách rỗng thì chỉ cần update lại adapter
            return;
        }
        final int[] count = {0};
        for (String productId : productIds) {
            db.collection("items")
                    .document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        count[0]++;
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            product.setId(documentSnapshot.getId()); // Bắt buộc
                            boolean rated = false;
                            if (product.getReviews() != null) {
                                for (Product.Review review : product.getReviews()) {
                                    if (review.getBuyerId().equals(userId)) {
                                        rated = true;
                                        break;
                                    }
                                }
                            }
                            if (!rated) {
                                productList.add(product);
                            }
                        }
                        // Khi đã lấy xong hết thì update adapter
                        if (count[0] == productIds.size()) {
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }
}
