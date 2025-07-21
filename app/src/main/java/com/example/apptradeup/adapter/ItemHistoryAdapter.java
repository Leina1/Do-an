package com.example.apptradeup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.apptradeup.OrderHistoryModel;
import com.example.apptradeup.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemHistoryAdapter extends RecyclerView.Adapter<ItemHistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<OrderHistoryModel> historyList;
    private String type; // "buy" hoặc "sell"

    public ItemHistoryAdapter(Context context, List<OrderHistoryModel> historyList, String type) {
        this.context = context;
        this.historyList = historyList;
        this.type = type;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        OrderHistoryModel order = historyList.get(position);

        // Tổng hợp tên sản phẩm và số lượng
        StringBuilder sb = new StringBuilder();
        int totalQty = 0;
        String imageUrl = null;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderHistoryModel.ItemInOrder item : order.getItems()) {
                sb.append(item.getTitle()).append(" x").append(item.getQuantity()).append(", ");
                totalQty += item.getQuantity();
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 2); // Xóa dấu phẩy cuối
            imageUrl = order.getItems().get(0).getImageUrl();
        }
        holder.tvItemName.setText(sb.length() > 0 ? sb.toString() : "Không có sản phẩm");
        holder.tvItemQuantity.setText("Số lượng: " + totalQty);

        // Ưu tiên lấy totalAmount (nếu có), nếu không thì lấy total gốc
        double displayPrice = (order.getTotalAmount() > 0) ? order.getTotalAmount() : order.getTotal();
        holder.tvItemPrice.setText(String.format("%,.0fđ", displayPrice));

        holder.tvItemDate.setText("Ngày: " + formatTimestamp(order.getTimestamp()));

        // Hiển thị trạng thái
        if ("buy".equals(type)) {
            holder.tvItemStatus.setText("Trạng thái: Đã nhận");
        } else if ("sell".equals(type)) {
            holder.tvItemStatus.setText("Trạng thái: Đã giao");
        } else {
            holder.tvItemStatus.setText("Trạng thái: " + (order.isCompleted() ? "Hoàn thành" : "Đang xử lý"));
        }

        // Ảnh sản phẩm đầu tiên hoặc default
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.imgProduct.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
        }
    }


    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvItemName, tvItemDate, tvItemStatus, tvItemQuantity, tvItemPrice;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDate = itemView.findViewById(R.id.tvItemDate);
            tvItemStatus = itemView.findViewById(R.id.tvItemStatus);
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
        }
    }

    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
