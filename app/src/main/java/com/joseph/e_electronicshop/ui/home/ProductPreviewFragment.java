package com.joseph.e_electronicshop.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.joseph.e_electronicshop.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProductPreviewFragment extends Fragment {




    private ImageView previewImageView;
    private TextView productNameTextView, priceKshTextView, priceUsdTextView,
            discountTextView, discountTypeTextView, descriptionTextView;
    private Button confirmButton;
    private ProgressBar previewProgressBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof AppCompatActivity)) {
            throw new IllegalStateException("Fragment must attach to AppCompatActivity");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadProductData();
        setupConfirmButton();
    }

    private void initViews(View view) {
        previewImageView = view.findViewById(R.id.previewImageView);
        productNameTextView = view.findViewById(R.id.productNameTextView);
        priceKshTextView = view.findViewById(R.id.priceKshTextView);
        priceUsdTextView = view.findViewById(R.id.priceUsdTextView);
        discountTextView = view.findViewById(R.id.discountTextView);
        discountTypeTextView = view.findViewById(R.id.discountTypeTextView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        confirmButton = view.findViewById(R.id.confirmButton);
        previewProgressBar = view.findViewById(R.id.previewProgressBar);
    }

    private void loadProductData() {
        try {
            Bundle args = getArguments();
            if (args == null) throw new Exception("No product data received");

            productNameTextView.setText(String.format("Product: %s", args.getString("productName", "")));
            priceKshTextView.setText(String.format("Price (Ksh): %s", args.getString("priceKsh", "")));
            priceUsdTextView.setText(String.format("Price (USD): %s", args.getString("priceUsd", "")));
            discountTextView.setText(String.format("Discount: %s", args.getString("discount", "")));
            discountTypeTextView.setText(String.format("Discount Type: %s", args.getString("discountType", "")));
            descriptionTextView.setText(String.format("Description: %s", args.getString("description", "")));

            String imageBase64 = args.getString("imageBase64", "");
            if (!imageBase64.isEmpty()) {
                loadBase64Image(imageBase64);
            }
        } catch (Exception e) {
            Log.e("PreviewError", "Error loading data: " + e.getMessage());
            showToast("Failed to load product data");
            if (isAdded()) {
                requireActivity().onBackPressed();
            }
        }
    }

    private void loadBase64Image(String base64) {
        new Thread(() -> {
            try {
                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (bitmap != null) {
                            previewImageView.setImageBitmap(bitmap);
                        } else {
                            previewImageView.setImageResource(R.drawable.ic_broken_image);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("ImageLoad", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            if (isAdded()) {
                saveProductToFirestore();
            }
        });
    }

    private void saveProductToFirestore() {
        try {
            previewProgressBar.setVisibility(View.VISIBLE);
            confirmButton.setEnabled(false);

            Bundle args = getArguments();
            if (args == null) throw new Exception("No product data available");

            Map<String, Object> product = new HashMap<>();
            product.put("productName", args.getString("productName", ""));
            product.put("priceKsh", args.getString("priceKsh", ""));
            product.put("priceUsd", args.getString("priceUsd", ""));
            product.put("discount", args.getString("discount", ""));
            product.put("discountType", args.getString("discountType", ""));
            product.put("description", args.getString("description", ""));
            product.put("category", args.getString("category", ""));
            product.put("imageBase64", args.getString("imageBase64", ""));
            product.put("timestamp", System.currentTimeMillis());

            FirebaseFirestore.getInstance().collection("products")
                    .add(product)
                    .addOnCompleteListener(task -> {
                        previewProgressBar.setVisibility(View.GONE);
                        confirmButton.setEnabled(true);

                        if (isAdded()) {
                            if (task.isSuccessful()) {
                                showSuccessDialog();
                            } else {
                                showToast("Save failed: " + Objects.requireNonNull(task.getException()).getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("SaveError", "Error: " + e.getMessage());
            if (isAdded()) {
                previewProgressBar.setVisibility(View.GONE);
                confirmButton.setEnabled(true);
                showToast("Error saving product");
            }
        }
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Success")
                .setMessage("Product added successfully!")
                .setPositiveButton("OK", (dialog, which) -> {
                    if (isAdded()) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showToast(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}