package com.joseph.e_electronicshop.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.joseph.e_electronicshop.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI components
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
        // Product categories
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

        // Discount types
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
        // Permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) openImagePicker();
                    else Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
        );

        // Image picker launcher
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
                                Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
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
        // Validate image first
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Please upload a product image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get all input values
        String productName = productNameEditText.getText().toString().trim();
        String priceKsh = priceKshEditText.getText().toString().trim();
        String description = productDescriptionEditText.getText().toString().trim();
        String category = categoryDropdown.getText().toString().trim();
        String discount = discountEditText.getText().toString().trim();
        String discountType = discountTypeSpinner.getText().toString().trim();

        // Validate required fields
        if (productName.isEmpty() || priceKsh.isEmpty() || description.isEmpty() || category.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // =============================================
        // UPDATED: Show loading state (using compound drawables for standard Button)
        // =============================================
        saveProductButton.setEnabled(false);
        saveProductButton.setText("SAVING...");

        // Add loading spinner drawable to the left of text
        Drawable loadingIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_loading);
        if (loadingIcon != null) {
            loadingIcon.setTint(Color.WHITE); // Make icon white
            saveProductButton.setCompoundDrawablesWithIntrinsicBounds(loadingIcon, null, null, null);
        }
        saveProductButton.setCompoundDrawablePadding(16); // Add space between icon and text

        // Show progress bar
        imageUploadProgressBar.setVisibility(View.VISIBLE);

        // Convert image to Base64
        String base64Image = encodeImageToBase64(selectedImageUri);
        if (base64Image == null) {
            Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();

            // =============================================
            // UPDATED: Reset button state on image error
            // =============================================
            imageUploadProgressBar.setVisibility(View.GONE);
            saveProductButton.setEnabled(true);
            saveProductButton.setText("SAVE PRODUCT");
            saveProductButton.setCompoundDrawables(null, null, null, null);
            return;
        }

        // Create product data map
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

        // Save to Firestore
        firestore.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    // Create a custom dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.ColorfulDialogTheme);

                    // Set custom view with icon and colors
                    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.success_dialog, null);
                    builder.setView(dialogView);

                    // Find views
                    ImageView icon = dialogView.findViewById(R.id.success_icon);
                    Button homeButton = dialogView.findViewById(R.id.home_button);

                    // Make it colorful
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setShape(GradientDrawable.RECTANGLE);
                    drawable.setCornerRadius(32f);
                    drawable.setColors(new int[]{
                            Color.parseColor("#FF9A9E"),  // Pink
                            Color.parseColor("#FAD0C4"),  // Peach
                            Color.parseColor("#FFD1FF")   // Light purple
                    });
                    drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                    drawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                    dialogView.setBackground(drawable);

                    // Set success icon
                    icon.setImageResource(R.drawable.ic_success);

                    // Create and show dialog
                    AlertDialog dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();

                    // Set button click to go home
                    homeButton.setOnClickListener(v -> {
                        dialog.dismiss();
                        if (isAdded() && getView() != null) {
                            try {
                                NavHostFragment.findNavController(this).navigate(R.id.nav_home);
                            } catch (Exception e) {
                                Log.e("Navigation", "Error navigating home", e);
                            }
                        }
                    });

                    clearForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error saving product: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("FirestoreError", "Error saving product", e);

                    // =============================================
                    // UPDATED: Reset button state on failure
                    // =============================================
                    imageUploadProgressBar.setVisibility(View.GONE);
                    saveProductButton.setEnabled(true);
                    saveProductButton.setText("SAVE PRODUCT");
                    saveProductButton.setCompoundDrawables(null, null, null, null);
                })
                .addOnCompleteListener(task -> {
                    // =============================================
                    // UPDATED: Hide progress bar when complete
                    // (success case handled in dialog, failure case handled above)
                    // =============================================
                    imageUploadProgressBar.setVisibility(View.GONE);
                });
    }
    private void clearForm() {
        productNameEditText.setText("");
        priceKshEditText.setText("");
        priceUsdEditText.setText("");
        discountEditText.setText("");
        productDescriptionEditText.setText("");
        categoryDropdown.setText("");
        discountTypeSpinner.setText("");
        productImageView.setImageResource(R.drawable.ic_add_photo_placeholder);
        selectedImageUri = null;
    }
}