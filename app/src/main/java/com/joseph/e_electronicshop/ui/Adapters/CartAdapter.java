package com.joseph.e_electronicshop.ui.Adapters;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.joseph.e_electronicshop.databinding.ItemCartBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CartAdapter extends ListAdapter<Product, CartAdapter.CartViewHolder> {

    public interface OnCartItemClickListener {
        void onBuyClick(Product product);
    }

    private final OnCartItemClickListener listener;

    public CartAdapter(OnCartItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CartViewHolder(binding, listener);  // Pass listener to ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;
        private final OnCartItemClickListener listener;

        CartViewHolder(ItemCartBinding binding, OnCartItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Product product) {
            // Load image
            if (product.getImageBase64() != null) {
                byte[] imageBytes = Base64.decode(product.getImageBase64(), Base64.DEFAULT);
                Glide.with(itemView.getContext())
                        .load(imageBytes)
                        .into(binding.cartProductImage);
            }

            binding.cartProductName.setText(product.getProductName());
            binding.cartProductPrice.setText(String.format("Ksh %s", product.getPriceKsh()));

            // Format time added
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            String timeAdded = "Added: " + sdf.format(new Date(product.getCartTimestamp()));
            binding.cartTimeAdded.setText(timeAdded);

            binding.buyButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBuyClick(product);
                }
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