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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.databinding.FragmentCartBinding;
import com.joseph.e_electronicshop.ui.Adapters.CartAdapter;
import com.joseph.e_electronicshop.ui.Adapters.Product;
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
        // Implement purchase logic
        Toast.makeText(requireContext(), "Purchased: " + product.getProductName(), Toast.LENGTH_SHORT).show();

        // Remove from cart after purchase
        db.collection("products")
                .document(product.getId())
                .update("inCart", false, "cartTimestamp", 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}