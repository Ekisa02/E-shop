package com.joseph.e_electronicshop.ui.cart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.databinding.FragmentCartBinding;
import com.joseph.e_electronicshop.ui.Adapters.CartAdapter;
import com.joseph.e_electronicshop.ui.Adapters.Product;
import com.joseph.e_electronicshop.ui.shop.ShopFragment;


import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private CartAdapter cartAdapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        fetchCartItems();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(product -> {
            // Handle buy button click
            buyProduct(product);
        });

        binding.cartRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.cartRecyclerView.setAdapter(cartAdapter);
    }

    private void fetchCartItems() {
        db.collection("products")
                .whereEqualTo("inCart", true)
                .orderBy("cartTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("CartFragment", "Error fetching cart items", error);
                        return;
                    }

                    List<Product> cartItems = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setId(doc.getId());
                                cartItems.add(product);
                            }
                        }
                    }

                    cartAdapter.submitList(cartItems);
                    binding.emptyCartText.setVisibility(cartItems.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void buyProduct(Product product) {
        // Create bundle with all necessary product data
        Bundle args = new Bundle();
        args.putString("productId", product.getId());
        args.putString("productName", product.getProductName());
        args.putString("productPrice", product.getPriceKsh());
        args.putString("productImage", product.getImageBase64());
        args.putString("productDescription", product.getDescription());

        // Create and attach arguments to ShopFragment
        ShopFragment shopFragment = new ShopFragment();
        shopFragment.setArguments(args);

        // Navigate to ShopFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.tab_fragment_container, shopFragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}