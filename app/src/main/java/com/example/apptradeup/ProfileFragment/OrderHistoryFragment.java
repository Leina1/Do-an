package com.example.apptradeup.ProfileFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.adapter.ItemHistoryAdapter;
import com.example.apptradeup.OrderHistoryModel;
import com.example.apptradeup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemHistoryAdapter adapter;
    private List<OrderHistoryModel> orderList = new ArrayList<>();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);
        recyclerView = view.findViewById(R.id.recyclerOrderHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lấy userId hiện tại
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Lấy dữ liệu Firestore
        FirebaseFirestore.getInstance()
                .collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderHistoryModel order = doc.toObject(OrderHistoryModel.class);
                        // Chỉ hiện các đơn đã hoàn thành và đúng buyerId
                        if (order != null && order.isCompleted() && userId.equals(order.getBuyerId())) {
                            orderList.add(order);
                        }
                    }
                    // Truyền orderList và type "buy" vào Adapter
                    adapter = new ItemHistoryAdapter(getContext(), orderList, "buy");
                    recyclerView.setAdapter(adapter);
                });

        return view;
    }
}
