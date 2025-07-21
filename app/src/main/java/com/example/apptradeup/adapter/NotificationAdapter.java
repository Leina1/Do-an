package com.example.apptradeup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.NotificationOrderModel;
import com.example.apptradeup.R;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationOrderModel> orders;
    private Context context;
    private OnActionListener listener;
    private boolean isSeller; // true nếu là người bán

    public void setSeller(boolean seller) {
        this.isSeller = seller;
    }

    public NotificationAdapter(Context context, List<NotificationOrderModel> orders, boolean isSeller, OnActionListener listener) {
        this.context = context;
        this.orders = orders;
        this.isSeller = isSeller;
        this.listener = listener;
    }

    public void setData(List<NotificationOrderModel> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        NotificationOrderModel order = orders.get(position);
        boolean isSeller = order.isSellerNow;

        holder.tvBuyerName.setText("Tên: " + order.buyerName + " (" + order.buyerPhone + ")");
        holder.tvBuyerAddress.setText("Địa chỉ: " + order.buyerAddress);
        holder.tvProductInfo.setText("Sản phẩm: " + order.productName + " x " + order.quantity);

        // Nếu là offer thì show thêm discount/totalAmount, nếu ko thì chỉ show tổng tiền
        if ("offer".equals(order.orderType)) {
            holder.tvTotalPrice.setText("Tổng gốc: " + String.format("%,.0f", order.totalPrice) + "đ\n"
                    + "Giảm giá: " + String.format("%,.0f", order.discount) + "đ\n"
                    + "Còn lại: " + String.format("%,.0f", order.totalAmount) + "đ");
        } else {
            holder.tvTotalPrice.setText("Tổng tiền: " + String.format("%,.0f", order.totalPrice) + "đ");
        }

        // Nếu là offer và bị từ chối (dành cho cả seller và buyer, ẩn mọi nút)
        if ("offer".equals(order.orderType) && order.offerRejected) {
            holder.tvOrderStatus.setText("Người bán đã từ chối ưu đãi");
            holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_red_dark));
            holder.btnSellerConfirm.setVisibility(View.GONE);
            holder.btnBuyerConfirm.setVisibility(View.GONE);
            if (holder.btnSellerReject != null) holder.btnSellerReject.setVisibility(View.GONE);
            return;
        }

        if (isSeller) {
            if ("offer".equals(order.orderType) && !order.sellerConfirmed) {
                // Đơn offer, chưa xác nhận, chưa từ chối
                holder.tvOrderStatus.setText("Đơn ưu đãi - Chờ xác nhận hoặc từ chối");
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark));
                holder.btnSellerConfirm.setVisibility(View.VISIBLE);
                if (holder.btnSellerReject != null) holder.btnSellerReject.setVisibility(View.VISIBLE);
                holder.btnBuyerConfirm.setVisibility(View.GONE);
            } else if (!order.sellerConfirmed) {
                holder.tvOrderStatus.setText("Chờ xác nhận");
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark));
                holder.btnSellerConfirm.setVisibility(View.VISIBLE);
                if (holder.btnSellerReject != null) holder.btnSellerReject.setVisibility(View.GONE);
                holder.btnBuyerConfirm.setVisibility(View.GONE);
            } else if (!order.completed) {
                holder.tvOrderStatus.setText("Đang giao hàng (chờ người mua xác nhận)");
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_blue_dark));
                holder.btnSellerConfirm.setVisibility(View.GONE);
                if (holder.btnSellerReject != null) holder.btnSellerReject.setVisibility(View.GONE);
                holder.btnBuyerConfirm.setVisibility(View.GONE);
            } else {
                holder.tvOrderStatus.setText("Đơn đã hoàn tất");
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_green_dark));
                holder.btnSellerConfirm.setVisibility(View.GONE);
                if (holder.btnSellerReject != null) holder.btnSellerReject.setVisibility(View.GONE);
                holder.btnBuyerConfirm.setVisibility(View.GONE);
            }
        } else {
            // Vai buyer
            if ("offer".equals(order.orderType) && order.offerRejected) {
                // Đã xử lý phía trên, nhưng có thể thừa, vẫn giữ để đảm bảo logic
                holder.tvOrderStatus.setText("Người bán đã từ chối ưu đãi");
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_red_dark));
                holder.btnSellerConfirm.setVisibility(View.GONE);
                holder.btnBuyerConfirm.setVisibility(View.GONE);
                if (holder.btnSellerReject != null) holder.btnSellerReject.setVisibility(View.GONE);
            } else if (!order.sellerConfirmed) {
                holder.tvOrderStatus.setText("Đang đợi xác nhận của người bán");
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark));
                holder.btnSellerConfirm.setVisibility(View.GONE);
                holder.btnBuyerConfirm.setVisibility(View.GONE);
                if (holder.btnSellerReject != null) holder.btnSellerReject.setVisibility(View.GONE);
            } else if (!order.completed) {
                holder.tvOrderStatus.setText("Đang giao hàng");
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_blue_dark));
                holder.btnSellerConfirm.setVisibility(View.GONE);
                holder.btnBuyerConfirm.setVisibility(View.VISIBLE);
                if (holder.btnSellerReject != null) holder.btnSellerReject.setVisibility(View.GONE);
            } else {
                holder.tvOrderStatus.setText("Đơn đã hoàn tất");
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_green_dark));
                holder.btnSellerConfirm.setVisibility(View.GONE);
                holder.btnBuyerConfirm.setVisibility(View.GONE);
                if (holder.btnSellerReject != null) holder.btnSellerReject.setVisibility(View.GONE);
            }
        }

        // Set click
        holder.btnSellerConfirm.setOnClickListener(v -> {
            if (listener != null) listener.onSellerConfirm(order);
        });
        if (holder.btnSellerReject != null) {
            holder.btnSellerReject.setOnClickListener(v -> {
                if (listener != null) listener.onSellerReject(order);
            });
        }
        holder.btnBuyerConfirm.setOnClickListener(v -> {
            if (listener != null) listener.onBuyerConfirm(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBuyerName, tvBuyerAddress, tvProductInfo, tvTotalPrice, tvOrderStatus;
        Button btnSellerConfirm, btnBuyerConfirm, btnSellerReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBuyerName = itemView.findViewById(R.id.tvBuyerName);
            tvBuyerAddress = itemView.findViewById(R.id.tvBuyerAddress);
            tvProductInfo = itemView.findViewById(R.id.tvProductInfo);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnSellerConfirm = itemView.findViewById(R.id.btnSellerConfirm);
            btnBuyerConfirm = itemView.findViewById(R.id.btnBuyerConfirm);
            // Nếu có reject thì mapping, không có thì null, vẫn chạy OK
            btnSellerReject = itemView.findViewById(R.id.btnSellerReject);
        }
    }

    // Hỗ trợ reject cho offer (nếu muốn gọi từ fragment)
    public interface OnActionListener {
        void onSellerConfirm(NotificationOrderModel order);
        void onSellerReject(NotificationOrderModel order);
        void onBuyerConfirm(NotificationOrderModel order);
    }
}
