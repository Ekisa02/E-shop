package com.joseph.e_electronicshop.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.joseph.e_electronicshop.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AdminpageFragment extends Fragment {

    // UI Components
    private ImageView productImageView;
    private Button uploadPhotoButton, saveProductButton;
    private ProgressBar imageUploadProgressBar;
    private Uri selectedImageUri;
    private TextInputEditText productNameEditText, priceKshEditText, priceUsdEditText,
            discountEditText, productDescriptionEditText;
    private AutoCompleteTextView discountTypeSpinner, categoryDropdown;

    // Firebase
    private FirebaseFirestore firestore;

    // Activity Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_adminpage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        initializeViews(view);
        setupDropdowns();
        setupButtonListeners();
        setupActivityLaunchers();
    }

    private void initializeViews(View view) {
        productImageView = view.findViewById(R.id.productImageView);
        uploadPhotoButton = view.findViewById(R.id.uploadPhotoButton);
        saveProductButton = view.findViewById(R.id.saveProductButton);
        imageUploadProgressBar = view.findViewById(R.id.imageUploadProgressBar);

        productNameEditText = view.findViewById(R.id.productNameEditText);
        priceKshEditText = view.findViewById(R.id.priceKshEditText);
        priceUsdEditText = view.findViewById(R.id.priceUsdEditText);
        discountEditText = view.findViewById(R.id.discountEditText);
        productDescriptionEditText = view.findViewById(R.id.productDescriptionEditText);
        discountTypeSpinner = view.findViewById(R.id.discountTypeSpinner);
        categoryDropdown = view.findViewById(R.id.categoryDropdown);
    }

    private void setupDropdowns() {
        String[] categories = {
                "Smartphones", "Laptops", "Televisions", "Headphones",
                "Cameras", "Accessories", "Gaming Consoles", "Smartwatches",
                "Home Appliances", "Networking Devices"
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        categoryDropdown.setAdapter(categoryAdapter);

        String[] discountTypes = {"Percentage", "Fixed Amount"};
        ArrayAdapter<String> discountAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                discountTypes
        );
        discountTypeSpinner.setAdapter(discountAdapter);
    }

    private void setupButtonListeners() {
        uploadPhotoButton.setOnClickListener(v -> checkPermissionAndPickImage());
        saveProductButton.setOnClickListener(v -> saveProductToFirestore());
    }

    private void setupActivityLaunchers() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) openImagePicker();
                    else showToast("Permission denied");
                }
        );

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                InputStream inputStream = requireContext().getContentResolver()
                                        .openInputStream(selectedImageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                productImageView.setImageBitmap(bitmap);
                                if (inputStream != null) inputStream.close();
                            } catch (Exception e) {
                                showToast("Error loading image");
                                Log.e("ImageError", "Error loading image", e);
                            }
                        }
                    }
                }
        );
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("ImageEncoding", "Error encoding image", e);
            return null;
        }
    }

    private void saveProductToFirestore() {
        if (selectedImageUri == null) {
            showToast("Please upload a product image");
            return;
        }

        String productName = productNameEditText.getText().toString().trim();
        String priceKsh = priceKshEditText.getText().toString().trim();
        String description = productDescriptionEditText.getText().toString().trim();
        String category = categoryDropdown.getText().toString().trim();
        String discount = discountEditText.getText().toString().trim();
        String discountType = discountTypeSpinner.getText().toString().trim();

        if (productName.isEmpty() || priceKsh.isEmpty() || description.isEmpty() || category.isEmpty()) {
            showToast("Please fill all required fields");
            return;
        }

        setLoadingState(true);

        String base64Image = encodeImageToBase64(selectedImageUri);
        if (base64Image == null) {
            showToast("Error processing image");
            setLoadingState(false);
            return;
        }

        Map<String, Object> product = new HashMap<>();
        product.put("productName", productName);
        product.put("priceKsh", priceKsh);
        product.put("priceUsd", priceUsdEditText.getText().toString());
        product.put("discount", discount);
        product.put("discountType", discountType);
        product.put("description", description);
        product.put("category", category);
        product.put("imageBase64", base64Image);
        product.put("timestamp", System.currentTimeMillis());

        firestore.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            setLoadingState(false);
                            showSuccessDialog();
                            clearForm();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            setLoadingState(false);
                            showToast("Error saving product: " + e.getMessage());
                            Log.e("FirestoreError", "Error saving product", e);
                        });
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                saveProductButton.setEnabled(!isLoading);
                imageUploadProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                saveProductButton.setText(isLoading ? "SAVING..." : "SAVE PRODUCT");

                if (isLoading) {
                    Drawable loadingIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_loading);
                    if (loadingIcon != null) {
                        loadingIcon.setTint(Color.WHITE);
                        saveProductButton.setCompoundDrawablesWithIntrinsicBounds(loadingIcon, null, null, null);
                    }
                    saveProductButton.setCompoundDrawablePadding(16);
                } else {
                    saveProductButton.setCompoundDrawables(null, null, null, null);
                }
            });
        }
    }

    private void showSuccessDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.success_dialog, null);
            builder.setView(dialogView);

            ImageView icon = dialogView.findViewById(R.id.success_icon);
            Button homeButton = dialogView.findViewById(R.id.home_button);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(32f);
            drawable.setColors(new int[]{
                    Color.parseColor("#FF9A9E"),
                    Color.parseColor("#FAD0C4"),
                    Color.parseColor("#FFD1FF")
            });
            drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            drawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            dialogView.setBackground(drawable);

            icon.setImageResource(R.drawable.ic_success);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            homeButton.setOnClickListener(v -> {
                dialog.dismiss();
                navigateToHome();
            });

            if (!requireActivity().isFinishing() && !requireActivity().isDestroyed()) {
                dialog.show();
            }
        } catch (Exception e) {
            Log.e("DialogError", "Error showing success dialog", e);
            showToast("Product saved successfully!");
            navigateToHome();
        }
    }

    private void navigateToHome() {
        if (isAdded()) {
            try {
                NavHostFragment.findNavController(this).navigate(R.id.nav_home);
            } catch (Exception e) {
                Log.e("Navigation", "Error navigating home", e);
            }
        }
    }

    private void clearForm() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                productNameEditText.setText("");
                priceKshEditText.setText("");
                priceUsdEditText.setText("");
                discountEditText.setText("");
                productDescriptionEditText.setText("");
                categoryDropdown.setText("");
                discountTypeSpinner.setText("");
                productImageView.setImageResource(R.drawable.ic_add_photo_placeholder);
                selectedImageUri = null;
            });
        }
    }

    private void showToast(String message) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        productImageView = null;
        uploadPhotoButton = null;
        saveProductButton = null;
        imageUploadProgressBar = null;
    }
}