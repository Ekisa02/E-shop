package com.joseph.e_electronicshop.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.ui.Adapters.ProductAdapter;
import com.joseph.e_electronicshop.databinding.FragmentHomeContentBinding;
import com.joseph.e_electronicshop.ui.Adapters.Product;

import java.util.ArrayList;
import java.util.List;

public class HomeContentFragment extends Fragment {
    private FragmentHomeContentBinding binding;
    private ProductAdapter productAdapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeContentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        setupRecyclerView();

        // Observe ViewModel if still needed
        HomeViewModel vm = new ViewModelProvider(this).get(HomeViewModel.class);
        vm.getText().observe(getViewLifecycleOwner(), text -> {
            binding.textHome.setText(text);
            binding.textHome.setVisibility(View.VISIBLE);
        });
    }

    private void setupRecyclerView() {
        // Initialize adapter
        productAdapter = new ProductAdapter();

        // Configure RecyclerView
        binding.productsRecyclerView.setLayoutManager(
                new GridLayoutManager(requireContext(), 2)
        );
        binding.productsRecyclerView.setAdapter(productAdapter);

        // Fetch products
        fetchProducts();
    }

    private void fetchProducts() {
        db.collection("products")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
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

                    if (products.isEmpty()) {
                        binding.textHome.setText(R.string.no_products_available);
                        binding.textHome.setVisibility(View.VISIBLE);
                    } else {
                        binding.textHome.setVisibility(View.GONE);
                    }

                    productAdapter.submitList(products);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}