package com.example.apptradeup.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.FragmentAddListing.FragmentAddItem;
import com.example.apptradeup.FragmentAddListing.FragmentManageListings;
import com.example.apptradeup.Product;
import com.example.apptradeup.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ProductViewHolder>{
    private Context context;
    private List<Product> productList;
    private FragmentManageListings fragmentManageListings;
    private boolean isToastShowing = false;

    public ItemAdapter(Context context, List<Product> productList, FragmentManageListings fragmentManageListings) {
        this.context = context;
        this.productList = productList;
        this.fragmentManageListings = fragmentManageListings;
    }
    // Constructor
    public ItemAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    // ViewHolder class
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvStatus ,tvQty;
        ImageView imgThumbnail;
        ImageView btnMore;
        CardView productCard;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvQty = itemView.findViewById(R.id.tvQty);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            btnMore = itemView.findViewById(R.id.btnMore);
            productCard = itemView.findViewById(R.id.productCard);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.item_listing, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText(String.format("Price: %,.0f VNĐ", product.getPrice()));
        holder.tvStatus.setText(product.getStatus());
        holder.tvQty.setText("Qty: " + product.getQuantity());
        if ("Available".equals(product.getStatus())) {
            if (product.getQuantity() == 0) {
                holder.tvQty.setText("Sold Out");
                holder.tvStatus.setText("Sold");
                updateProductStatus(position, "Sold");
            } else {
                holder.tvStatus.setText("Available");
            }
        } else if ("Paused".equals(product.getStatus())) {
            holder.tvStatus.setText("Paused");
        } else if ("Sold".equals(product.getStatus())) {
            holder.tvStatus.setText("Sold");
            holder.tvQty.setText("Sold Out");
        }
        // Load image using Picasso
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Picasso.get().load(product.getImages().get(0)).into(holder.imgThumbnail);
        }

        // Handle More button (show popup menu)
        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);

            // Add items to the PopupMenu
            popupMenu.getMenuInflater().inflate(R.menu.product_options_menu, popupMenu.getMenu());

            // Change menu items title based on product status
            if ("Paused".equals(product.getStatus())) {
                popupMenu.getMenu().findItem(R.id.menu_pause).setTitle("Available"); // Change "Pause" to "Available"
            } else if ("Available".equals(product.getStatus())) {
                popupMenu.getMenu().findItem(R.id.menu_pause).setTitle("Pause"); // Change "Available" to "Pause"
            }
            else if ("Sold".equals(product.getStatus())) {
                popupMenu.getMenu().findItem(R.id.menu_pause).setTitle("Pause"); // Change "Sold" to "Pause"
            }

            // Set the event listener for each menu item
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_delete) {
                    deleteProduct(position);
                    return true;
                }
                else if (itemId == R.id.menu_edit){
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("product", product);  // Assuming Product implements Serializable

                    FragmentAddItem addItemFragment = new FragmentAddItem();
                    addItemFragment.setArguments(bundle);

                    // Load the fragment for editing
                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, addItemFragment)
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
                else if (itemId == R.id.menu_pause) {
                    if ("Available".equals(product.getStatus())) {
                        updateProductStatus(position, "Paused");
                    } else if ("Paused".equals(product.getStatus())) {
                        updateProductStatus(position, "Available");
                    } else if ("Sold".equals(product.getStatus())) {
                        updateProductStatus(position, "Paused");
                    }
                    return true;
                }
                return false;
            });
            // Show the menu
            popupMenu.show();
        });

        // Handle CardView click (optional, for product details)
        holder.productCard.setOnClickListener(v -> {
            // Handle product click to show more details
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Update data (useful for filtering)
    public void updateData(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }

    // Update the product status in Firebase

    private void updateProductStatus(int position, String newStatus) {
        Product selectedProduct = productList.get(position);

        String productId = selectedProduct.getId();
        if (productId == null) {
            Log.e("Update Error", "Product ID is null");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("items").document(productId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    selectedProduct.setStatus(newStatus);
                    notifyItemChanged(position);

                    // Chỉ hiển thị Toast nếu chưa hiển thị Toast trước đó
                    if (!isToastShowing) {
                        Toast.makeText(context, "Product status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                        isToastShowing = true;
                    }

                    if (fragmentManageListings != null) {
                        fragmentManageListings.filterProducts(fragmentManageListings.getSelectedTabPosition());
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error updating product status", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteProduct(int position) {
        Product selectedProduct = productList.get(position);
        String productId = selectedProduct.getId();

        if (productId == null) {
            Log.e("Delete Error", "Product ID is null");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("items").document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    productList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();

                    if (fragmentManageListings != null) {
                        fragmentManageListings.filterProducts(fragmentManageListings.getSelectedTabPosition());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error deleting product", Toast.LENGTH_SHORT).show();
                });
    }
}
