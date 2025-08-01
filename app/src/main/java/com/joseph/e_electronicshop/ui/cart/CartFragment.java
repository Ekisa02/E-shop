package com.joseph.e_electronicshop.ui.cart;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.databinding.FragmentCartBinding;
import com.joseph.e_electronicshop.ui.Adapters.CartAdapter;
import com.joseph.e_electronicshop.ui.Adapters.Product;
import com.joseph.e_electronicshop.ui.shop.ShopFragment;
import com.joseph.e_electronicshop.ui.viewmodels.SharedProductViewModel;
import com.joseph.e_electronicshop.ui.viewmodels.SharedViewModel;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartFragment extends Fragment {

    // 🔽 Empty constructor
    public CartFragment() {
        // Required empty public constructor
    }


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



       SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.getSelectedProduct().observe(getViewLifecycleOwner(), product -> {
            if (product != null && product.isInCart()) {
                // Optional: trigger manual refresh
                fetchCartItems();
            }
        });

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
                        Log.e("CartFragment", "Error fetching cart items: ", error);
                        Toast.makeText(requireContext(), "Error loading cart items.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        cartAdapter.submitList(Collections.emptyList());
                        binding.emptyCartText.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<Product> cartItems = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            product.setId(doc.getId());
                            cartItems.add(product);
                        } else {
                            Log.w("CartFragment", "Skipped null product document: " + doc.getId());
                        }
                    }

                    cartAdapter.submitList(cartItems);
                    binding.emptyCartText.setVisibility(cartItems.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }


    private void buyProduct(Product product) {
        ProgressDialog dialog = new ProgressDialog(requireContext());
        dialog.setMessage("Sending to shop...");
        Toast.makeText(getContext(), "Navigate to Shop Tap to Proceed!", Toast.LENGTH_SHORT).show();
        dialog.setCancelable(false);
        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedProductViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedProductViewModel.class);
            viewModel.setProduct(product);

            // Switch to Shop tab
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_nav);
            nav.setSelectedItemId(R.id.nav_shop); // or whatever your menu ID is

            dialog.dismiss();
        }, 1000);
    }






    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}