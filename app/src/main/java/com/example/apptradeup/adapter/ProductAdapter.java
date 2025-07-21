package com.example.apptradeup.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.Product;
import com.example.apptradeup.ProductDetailActivity;
import com.example.apptradeup.R;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.productList = products;
    }
    public List<Product> getCurrentList() {
        return new ArrayList<>(this.productList);
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtTitle, txtPrice, txtSold, txtLocation;

        public ProductViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtTitle = itemView.findViewById(R.id.txtProductTitle);
            txtPrice = itemView.findViewById(R.id.txtProductPrice);
            txtSold = itemView.findViewById(R.id.txtSold);
            txtLocation = itemView.findViewById(R.id.txtLocation);
        }

        public void bind(Product product) {
            txtTitle.setText(product.getTitle());
            txtPrice.setText(String.format("%.0fđ", product.getPrice()));
            txtSold.setText("Đã bán: " + product.getSold());
            txtLocation.setText(product.getLocation());

            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Picasso.get().load(product.getImages().get(0)).into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.ic_launcher_background);
            }
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ProductDetailActivity.class);
                if (product.getId() != null) {
                    intent.putExtra("itemId", product.getId());
                    itemView.getContext().startActivity(intent);
                } else {
                    Toast.makeText(itemView.getContext(), "ID sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }
}
