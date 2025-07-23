package com.joseph.e_electronicshop.ui.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.databinding.ItemProductCardBinding;
import com.joseph.e_electronicshop.ui.Adapters.Product;

public class ProductAdapter extends ListAdapter<Product, ProductAdapter.ProductViewHolder> {

    public ProductAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductCardBinding binding = ItemProductCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductCardBinding binding;

        ProductViewHolder(ItemProductCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            // Load image using Glide
            if (product.getImageBase64() != null && !product.getImageBase64().isEmpty()) {
                // If using Base64 images (decode first)
                byte[] imageBytes = android.util.Base64.decode(product.getImageBase64(), android.util.Base64.DEFAULT);
                Glide.with(itemView.getContext())
                        .load(imageBytes)
                        .placeholder(R.drawable.placeholder_product)
                        .into(binding.productImage);
            }

            // Set product details
            binding.productName.setText(product.getProductName());
            binding.productPrice.setText(String.format("Ksh %s", product.getPriceKsh()));

            // Show discount if available
            if (product.getDiscount() != null && !product.getDiscount().isEmpty()) {
                binding.productDiscount.setText(String.format("%s OFF", product.getDiscount()));
                binding.productDiscount.setVisibility(View.VISIBLE);
            } else {
                binding.productDiscount.setVisibility(View.GONE);
            }

            binding.addToCartButton.setOnClickListener(v -> {
                // Handle add to cart
            });
        }
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Product>() {
                @Override
                public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return oldItem.equals(newItem);
                }
            };
}