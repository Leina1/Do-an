package com.example.apptradeup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.apptradeup.Product;
import com.example.apptradeup.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ItemCartAdapter extends RecyclerView.Adapter<ItemCartAdapter.CartViewHolder> {

    private List<Product> cartList;
    private Map<String, Integer> quantities = new HashMap<>();
    private HashSet<String> selectedIds = new HashSet<>();

    private OnProductSelectListener listener;

    public interface OnProductSelectListener {
        void onProductSelectionChanged();
    }

    public ItemCartAdapter(List<Product> cartList, OnProductSelectListener listener) {
        this.cartList = cartList;
        this.listener = listener;

        for (Product product : cartList) {
            quantities.put(product.getId(), 1); // Mặc định mỗi sản phẩm có số lượng là 1
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartList.get(position);
        String productId = product.getId();
        int quantity = quantities.get(productId);
        double totalPrice = product.getPrice() * quantity;

        holder.txtTitle.setText(product.getTitle());
        holder.txtQuantity.setText(String.valueOf(quantity));
        holder.txtPrice.setText(String.format("%,.0f VNĐ", totalPrice));

        Glide.with(holder.imgProduct.getContext())
                .load(product.getImages() != null && !product.getImages().isEmpty()
                        ? product.getImages().get(0)
                        : R.drawable.ic_launcher_background)
                .into(holder.imgProduct);

        // Gán lại checkbox đúng trạng thái
        holder.checkBox.setOnCheckedChangeListener(null); // tránh callback khi setChecked
        holder.checkBox.setChecked(selectedIds.contains(productId));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedIds.add(productId);
            } else {
                selectedIds.remove(productId);
            }
            if (listener != null) {
                listener.onProductSelectionChanged();
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            quantities.put(productId, quantity + 1);
            notifyItemChanged(position);
            if (listener != null && selectedIds.contains(productId)) {
                listener.onProductSelectionChanged();
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantities.put(productId, quantity - 1);
                notifyItemChanged(position);
                if (listener != null && selectedIds.contains(productId)) {
                    listener.onProductSelectionChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtTitle, txtPrice, txtQuantity;
        CheckBox checkBox;
        ImageButton btnRemove, btnIncrease, btnDecrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtTitle = itemView.findViewById(R.id.txtProductTitle);
            txtPrice = itemView.findViewById(R.id.txtProductPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            checkBox = itemView.findViewById(R.id.checkBoxSelectProduct);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }

    // Lấy danh sách sản phẩm đã chọn
    public List<Product> getSelectedProducts() {
        List<Product> selected = new ArrayList<>();
        for (Product p : cartList) {
            if (selectedIds.contains(p.getId())) {
                selected.add(p);
            }
        }
        return selected;
    }

    // Lấy số lượng sản phẩm đã chọn (dùng trong tính tổng)
    public int getQuantityFor(String productId) {
        return quantities.get(productId);
    }

    // Trả về tất cả quantity
    public Map<String, Integer> getQuantities() {
        return quantities;
    }
    public void selectAll(boolean select) {
        selectedIds.clear();
        if (select) {
            for (Product product : cartList) {
                selectedIds.add(product.getId());
            }
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onProductSelectionChanged();
        }
    }
}
