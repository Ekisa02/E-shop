package com.joseph.e_electronicshop.ui.Adapters;

import android.graphics.Paint;
import android.os.Handler;
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
import java.util.Locale;

public class ProductAdapter extends ListAdapter<Product, ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onAddToCartClick(Product product);
        void onProductExpired(Product product);
    }

    private final OnProductClickListener listener;

    public ProductAdapter(OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductCardBinding binding = ItemProductCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ProductViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductCardBinding binding;
        private final OnProductClickListener listener;
        private final Handler handler = new Handler();
        private Runnable expirationRunnable;

        ProductViewHolder(ItemProductCardBinding binding, OnProductClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Product product) {
            // Clear any pending expiration runnable
            if (expirationRunnable != null) {
                handler.removeCallbacks(expirationRunnable);
            }

            // Load image using Glide
            if (product.getImageBase64() != null && !product.getImageBase64().isEmpty()) {
                byte[] imageBytes = android.util.Base64.decode(product.getImageBase64(), android.util.Base64.DEFAULT);
                Glide.with(itemView.getContext())
                        .load(imageBytes)
                        .placeholder(R.drawable.placeholder_product)
                        .into(binding.productImage);
            }

            // Set product details
            binding.productName.setText(product.getProductName());
            binding.productPrice.setText(String.format("Ksh %s", product.getPriceKsh()));

            // Set timestamp
            binding.productTimestamp.setText(getTimeAgo(product.getTimestamp()));

            // Show discount if available
            if (product.getDiscount() != null && !product.getDiscount().isEmpty()) {
                binding.productDiscount.setText(String.format("%s OFF", product.getDiscount()));
                binding.productDiscount.setVisibility(View.VISIBLE);
            } else {
                binding.productDiscount.setVisibility(View.GONE);
            }

            // Handle in-cart state
            if (product.isInCart()) {
                binding.getRoot().setAlpha(0.5f);
                binding.productName.setPaintFlags(binding.productName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.productPrice.setPaintFlags(binding.productPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                // Schedule removal after 1 hour
                long timeLeft = 3600000 - (System.currentTimeMillis() - product.getCartTimestamp());
                if (timeLeft > 0) {
                    expirationRunnable = () -> {
                        if (listener != null) {
                            listener.onProductExpired(product);
                        }
                    };
                    handler.postDelayed(expirationRunnable, timeLeft);
                }
            } else {
                binding.getRoot().setAlpha(1f);
                binding.productName.setPaintFlags(binding.productName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.productPrice.setPaintFlags(binding.productPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            // Set click listeners
            binding.productImage.setOnClickListener(v -> {
                if (binding.productDescription.getVisibility() == View.VISIBLE) {
                    binding.productDescription.setVisibility(View.GONE);
                } else {
                    binding.productDescription.setText(product.getDescription());
                    binding.productDescription.setVisibility(View.VISIBLE);
                }
            });

            binding.addToCartButton.setOnClickListener(v -> {
                if (!product.isInCart() && listener != null) {
                    listener.onAddToCartClick(product);
                }
            });
        }

        private String getTimeAgo(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days == 1 ? "Posted 1 day ago" : String.format(Locale.getDefault(), "Posted %d days ago", days);
            } else if (hours > 0) {
                return hours == 1 ? "Posted 1 hour ago" : String.format(Locale.getDefault(), "Posted %d hours ago", hours);
            } else if (minutes > 0) {
                return minutes == 1 ? "Posted 1 minute ago" : String.format(Locale.getDefault(), "Posted %d minutes ago", minutes);
            } else {
                return "Posted just now";
            }
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
                    return oldItem.getProductName().equals(newItem.getProductName()) &&
                            oldItem.getPriceKsh().equals(newItem.getPriceKsh()) &&
                            oldItem.getDiscount().equals(newItem.getDiscount()) &&
                            oldItem.getDescription().equals(newItem.getDescription()) &&
                            oldItem.isInCart() == newItem.isInCart() &&
                            oldItem.getImageBase64().equals(newItem.getImageBase64()) &&
                            oldItem.getTimestamp() == newItem.getTimestamp();
                }
            };
}