package com.example.apptradeup.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apptradeup.NotificationOrderModel;
import com.example.apptradeup.R;
import com.example.apptradeup.adapter.NotificationAdapter;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView tvNoNotifications;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(getContext(), new ArrayList<>(), false, new NotificationAdapter.OnActionListener() {
            @Override
            public void onSellerConfirm(NotificationOrderModel order) {
                db.collection("orders").document(order.orderId)
                        .update("sellerConfirmed", true)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Đã xác nhận giao hàng!", Toast.LENGTH_SHORT).show();
                            loadNotifications();
                        });
            }
            @Override
            public void onBuyerConfirm(NotificationOrderModel order) {
                db.collection("orders").document(order.orderId)
                        .update("buyerConfirmed", true, "completed", true)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Đã xác nhận nhận hàng!", Toast.LENGTH_SHORT).show();
                            loadNotifications();
                        });
            }
            @Override
            public void onSellerReject(NotificationOrderModel order) {
                db.collection("orders").document(order.orderId)
                        .update("offerRejected", true)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Đã từ chối ưu đãi!", Toast.LENGTH_SHORT).show();
                            loadNotifications();
                        });
            }
        });
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            currentUserId = getArguments().getString("userId");
        }

        loadNotifications();
        return view;
    }

    private void loadNotifications() {
        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<NotificationOrderModel> notificationOrders = new ArrayList<>();
            List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
            List<Map<String, Object>> taskParams = new ArrayList<>();
            final boolean[] userIsSeller = {false};

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String buyerId = doc.getString("buyerId");
                List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                boolean sellerConfirmed = doc.getBoolean("sellerConfirmed") != null && doc.getBoolean("sellerConfirmed");
                boolean buyerConfirmed = doc.getBoolean("buyerConfirmed") != null && doc.getBoolean("buyerConfirmed");
                boolean completed = doc.getBoolean("completed") != null && doc.getBoolean("completed");
                String orderId = doc.getId();

                String orderType = doc.getString("orderType") != null ? doc.getString("orderType") : "normal";
                double discount = doc.getDouble("discount") != null ? doc.getDouble("discount") : 0;
                double totalAmount = doc.getDouble("totalAmount") != null ? doc.getDouble("totalAmount") : 0;
                boolean offerRejected = doc.getBoolean("offerRejected") != null && doc.getBoolean("offerRejected");

                for (Map<String, Object> item : items) {
                    String productName = (String) item.get("title");
                    int quantity = ((Long) item.get("quantity")).intValue();
                    double price = (double) item.get("price");
                    String sellerId = (String) item.get("sellerId");

                    boolean isSellerNow = (sellerId != null && currentUserId != null && currentUserId.equals(sellerId));
                    boolean isBuyerNow = (currentUserId != null && currentUserId.equals(buyerId));

                    // Chỉ thêm 1 lần duy nhất, ưu tiên seller
                    if (isSellerNow) {
                        Task<DocumentSnapshot> userTask = db.collection("users").document(buyerId).get();
                        userTasks.add(userTask);

                        Map<String, Object> param = new HashMap<>();
                        param.put("orderId", orderId);
                        param.put("buyerId", buyerId);
                        param.put("productName", productName);
                        param.put("quantity", quantity);
                        param.put("price", price);
                        param.put("sellerConfirmed", sellerConfirmed);
                        param.put("buyerConfirmed", buyerConfirmed);
                        param.put("completed", completed);
                        param.put("isSellerNow", true); // role seller
                        param.put("orderType", orderType);
                        param.put("discount", discount);
                        param.put("totalAmount", totalAmount > 0 ? totalAmount : (price * quantity - discount));
                        param.put("offerRejected", offerRejected);

                        taskParams.add(param);
                        // ưu tiên seller, bỏ buyer
                    } else if (isBuyerNow) {
                        Task<DocumentSnapshot> userTask = db.collection("users").document(buyerId).get();
                        userTasks.add(userTask);

                        Map<String, Object> param = new HashMap<>();
                        param.put("orderId", orderId);
                        param.put("buyerId", buyerId);
                        param.put("productName", productName);
                        param.put("quantity", quantity);
                        param.put("price", price);
                        param.put("sellerConfirmed", sellerConfirmed);
                        param.put("buyerConfirmed", buyerConfirmed);
                        param.put("completed", completed);
                        param.put("isSellerNow", false); // role buyer
                        param.put("orderType", orderType);
                        param.put("discount", discount);
                        param.put("totalAmount", totalAmount > 0 ? totalAmount : (price * quantity - discount));
                        param.put("offerRejected", offerRejected);

                        taskParams.add(param);
                    }
                }
            }

            if (userTasks.isEmpty()) {
                adapter.setData(new ArrayList<>());
                tvNoNotifications.setVisibility(View.VISIBLE);
                return;
            }

            AtomicInteger finished = new AtomicInteger(0);
            for (int i = 0; i < userTasks.size(); i++) {
                final int idx = i;
                userTasks.get(i).addOnSuccessListener(buyerDoc -> {
                    Map<String, Object> param = taskParams.get(idx);
                    String buyerName = buyerDoc.getString("display_name");
                    String buyerPhone = buyerDoc.getString("phone");
                    String buyerAddress = buyerDoc.getString("address");

                    NotificationOrderModel model = new NotificationOrderModel(
                            (String) param.get("orderId"),
                            buyerName,
                            buyerPhone,
                            buyerAddress,
                            (String) param.get("productName"),
                            (int) param.get("quantity"),
                            ((double) param.get("price")) * (int) param.get("quantity"),
                            (boolean) param.get("sellerConfirmed"),
                            (boolean) param.get("buyerConfirmed"),
                            (boolean) param.get("completed"),
                            (boolean) param.get("isSellerNow"),
                            (String) param.get("orderType"),
                            (double) param.get("discount"),
                            (double) param.get("totalAmount"),
                            (boolean) param.get("offerRejected")
                    );
                    notificationOrders.add(model);

                    // Xác định role hiển thị
                    if ((boolean) param.get("isSellerNow")) userIsSeller[0] = true;

                    if (finished.incrementAndGet() == userTasks.size()) {
                        // Nếu là seller sẽ đổi style, ngược lại thì để buyer
                        adapter.setSeller(userIsSeller[0]);
                        adapter.setData(notificationOrders);
                        tvNoNotifications.setVisibility(notificationOrders.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            }
        });
    }
}
