package com.example.apptradeup.FragmentAddListing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptradeup.R;
import com.example.apptradeup.Product;
import com.example.apptradeup.adapter.ItemAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FragmentManageListings extends Fragment {
    private String userId;
    private TabLayout tabLayout;
    private ImageButton btnAddListing;
    private RecyclerView recyclerViewListings;
    private ItemAdapter itemAdapter; // Adapter để hiển thị danh sách sản phẩm
    private List<Product> allProducts = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_listings, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerViewListings = view.findViewById(R.id.recyclerViewListings);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        itemAdapter = new ItemAdapter(getContext(), new ArrayList<>(),this);
        recyclerViewListings.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewListings.setAdapter(itemAdapter);
        // Thêm 4 tabs
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Available"));
        tabLayout.addTab(tabLayout.newTab().setText("Sold"));
        tabLayout.addTab(tabLayout.newTab().setText("Paused"));

        loadProductsFromFirestore();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                filterProducts(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        btnAddListing= view.findViewById(R.id.btnAddProduct);
        btnAddListing.setOnClickListener(v -> {
            FragmentAddItem addItemFragment = new FragmentAddItem();
            Bundle bundle = new Bundle();
            bundle.putString("userID", userId);  // Truyền userId
            addItemFragment.setArguments(bundle);
            loadFragment(addItemFragment);
        });
        return view;
    }
    private void loadFragment(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)  // Đảm bảo ID đúng với container trong MainActivity
                .addToBackStack(null)  // Đảm bảo có thể quay lại trang trước
                .commit();
    }
    private void loadProductsFromFirestore() {
        allProducts.clear();
        FirebaseFirestore.getInstance().collection("items")
                .whereEqualTo("sellerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Lấy sản phẩm từ Firestore và gán ID
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId()); // Lấy ID từ Firestore và gán vào đối tượng Product
                        allProducts.add(product);
                    }
                    filterProducts(tabLayout.getSelectedTabPosition()); // Filter ban đầu
                });
    }
    public int getSelectedTabPosition() {
        return tabLayout.getSelectedTabPosition();
    }
    public void filterProducts(int tabPosition) {
        String status = "";
        switch (tabPosition) {
            case 0: // All
                itemAdapter.updateData(allProducts);
                return;
            case 1:
                status = "Available";
                break;
            case 2:
                status = "Sold";
                break;
            case 3:
                status = "Paused";
                break;
        }
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (status.equalsIgnoreCase(p.getStatus())) filtered.add(p);
        }
        itemAdapter.updateData(filtered);
    }
}
