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

import com.example.apptradeup.OrderHistoryModel; // Đổi đúng package model
import com.example.apptradeup.adapter.ItemHistoryAdapter; // Đổi đúng package Adapter
import com.example.apptradeup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SellHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemHistoryAdapter adapter;
    private List<OrderHistoryModel> sellList = new ArrayList<>();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sell_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerSellHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // Có thể hiện thông báo, chuyển về login...
            return view;
        }
        userId = auth.getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sellList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderHistoryModel order = doc.toObject(OrderHistoryModel.class);
                        if (order != null && order.isCompleted() && order.getItems() != null) {
                            // Duyệt từng item để kiểm tra sellerId
                            boolean isMyOrder = false;
                            for (OrderHistoryModel.ItemInOrder item : order.getItems()) {
                                if (userId.equals(item.getSellerId())) {
                                    isMyOrder = true;
                                    break;
                                }
                            }
                            if (isMyOrder) {
                                sellList.add(order);
                            }
                        }
                    }
                    adapter = new ItemHistoryAdapter(getContext(), sellList, "sell");
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi nếu cần
                });

        return view;
    }
}
