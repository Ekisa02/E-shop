package com.joseph.e_electronicshop.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.databinding.FragmentHomeContentBinding;
import com.joseph.e_electronicshop.ui.Adapters.Product;
import com.joseph.e_electronicshop.ui.Adapters.ProductAdapter;
import com.joseph.e_electronicshop.ui.viewmodels.SharedProductViewModel;
import com.joseph.e_electronicshop.ui.viewmodels.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeContentFragment extends Fragment {
    private FragmentHomeContentBinding binding;
    private ProductAdapter productAdapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeContentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        fetchProducts();

        HomeViewModel vm = new ViewModelProvider(this).get(HomeViewModel.class);
        vm.getText().observe(getViewLifecycleOwner(), text -> {
            binding.textHome.setText(text);
            binding.textHome.setVisibility(View.VISIBLE);
        });
    }
    private void showProgress(String message) {
        binding.progressContainer.setVisibility(View.VISIBLE);
        binding.progressText.setText(message);
    }

    private void hideProgress() {
        binding.progressContainer.setVisibility(View.GONE);
        binding.progressText.setText("");
    }


    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onAddToCartClick(Product product) {
                addToCart(product);
            }

            @Override
            public void onProductExpired(Product product) {
                removeFromCart(product);
            }
        });

        binding.productsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.productsRecyclerView.setAdapter(productAdapter);
    }

    private void fetchProducts() {
        showProgress("Loading products...");

        db.collection("products")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    hideProgress();

                    if (error != null) {
                        Log.e("HomeFragment", "Listen failed", error);
                        binding.textHome.setText(R.string.error_loading_products);
                        binding.textHome.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<Product> products = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setId(doc.getId());
                                products.add(product);
                            }
                        }
                    }

                    productAdapter.submitList(new ArrayList<>(products));
                    binding.textHome.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }


    private void addToCart(Product product) {
        if (product == null || product.isInCart()) {
            Toast.makeText(requireContext(), "Already in cart", Toast.LENGTH_SHORT).show();
            return;
        }

        product.setInCart(true);
        product.setCartTimestamp(System.currentTimeMillis());

        // 1. Save to Firestore
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(product.getId())
                .update("inCart", true, "cartTimestamp", product.getCartTimestamp())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Saved to Firestore", Toast.LENGTH_SHORT).show();

                    // 2. Pass to ViewModel after saving
                    SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                    viewModel.selectProduct(product);
                    BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_nav);
                    nav.setSelectedItemId(R.id.nav_cart); // or whatever your menu ID is
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error:", e);
                });
    }


    private void removeFromCart(Product product) {
        db.collection("products")
                .document(product.getId())
                .update("inCart", false, "cartTimestamp", 0)
                .addOnSuccessListener(aVoid -> {
                    // Product will automatically update due to Firestore listener
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}