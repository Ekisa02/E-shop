package com.joseph.e_electronicshop.ui.home;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminpageFragment extends Fragment {

    private static final String TAG = "AdminFragment";
    private static final int MAX_IMAGE_SIZE_KB = 1024; // 1MB max
    private static final int TARGET_WIDTH = 800;
    private static final int TARGET_HEIGHT = 800;
    private static final int QUALITY = 70;
    private static final int MIN_QUALITY = 30;
    private static final int QUALITY_STEP = 10;

    // UI Components
    private ImageView productImageView;
    private Button uploadPhotoButton, saveProductButton;
    private Uri selectedImageUri;
    private TextInputEditText productNameEditText, priceKshEditText, priceUsdEditText,
            discountEditText, productDescriptionEditText;
    private AutoCompleteTextView discountTypeSpinner, categoryDropdown;

    // Firebase
    private FirebaseFirestore firestore;

    // Threading
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ProgressDialog progressDialog;

    // Activity Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
    }

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
        initializeViews(view);
        setupDropdowns();
        setupActivityLaunchers();
        setupButtonListeners();
    }

    private void initializeViews(View view) {
        productImageView = view.findViewById(R.id.productImageView);
        uploadPhotoButton = view.findViewById(R.id.uploadPhotoButton);
        saveProductButton = view.findViewById(R.id.saveProductButton);

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
        saveProductButton.setOnClickListener(v -> saveProduct());
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
                            loadSelectedImage();
                        }
                    }
                }
        );
    }

    private void loadSelectedImage() {
        showProgressDialog("Loading image...");
        executorService.execute(() -> {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                options.inPreferredConfig = Bitmap.Config.RGB_565;

                try (InputStream input = requireContext().getContentResolver().openInputStream(selectedImageUri)) {
                    final Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
                    mainHandler.post(() -> {
                        if (isAdded()) {
                            productImageView.setImageBitmap(bitmap);
                        }
                        dismissProgressDialog();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Image loading failed", e);
                mainHandler.post(() -> {
                    dismissProgressDialog();
                    showToast("Failed to load image");
                });
            }
        });
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Image picker failed", e);
            showToast("Failed to open image picker");
        }
    }

    private void saveProduct() {
        if (!validateInputs()) return;

        showProgressDialog("Saving product...");
        executorService.execute(() -> {
            try {
                String base64Image = processAndCompressImage();
                if (base64Image == null) {
                    mainHandler.post(() -> {
                        dismissProgressDialog();
                        showToast("Failed to process image");
                    });
                    return;
                }

                Map<String, Object> product = createProductData(base64Image);
                saveToFirestore(product);
            } catch (Exception e) {
                Log.e(TAG, "Save product error", e);
                mainHandler.post(() -> {
                    dismissProgressDialog();
                    showToast("Error saving product");
                });
            }
        });
    }

    private String processAndCompressImage() {
        if (selectedImageUri == null) return null;

        try (InputStream input = requireContext().getContentResolver().openInputStream(selectedImageUri)) {
            // Check original size
            int originalSize = input.available();
            if (originalSize <= MAX_IMAGE_SIZE_KB * 1024) {
                // Small enough, use basic encoding
                return encodeImageToBase64(selectedImageUri);
            } else {
                // Needs compression
                return compressAndEncodeImage(selectedImageUri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Image processing failed", e);
            return null;
        }
    }

    private String compressAndEncodeImage(Uri imageUri) {
        Bitmap bitmap = null;
        try {
            // Get dimensions first
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream boundsInput = requireContext().getContentResolver().openInputStream(imageUri)) {
                BitmapFactory.decodeStream(boundsInput, null, options);
            }

            // Calculate scale factor
            int scaleFactor = Math.min(
                    options.outWidth / TARGET_WIDTH,
                    options.outHeight / TARGET_HEIGHT
            );
            scaleFactor = Math.max(1, scaleFactor);

            // Decode with scaling
            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            try (InputStream decodeInput = requireContext().getContentResolver().openInputStream(imageUri)) {
                bitmap = BitmapFactory.decodeStream(decodeInput, null, options);
            }

            if (bitmap == null) {
                Log.e(TAG, "Bitmap decoding failed after scaling");
                return null;
            }

            // Compress with quality adjustment
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int quality = QUALITY;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

            // Reduce quality if still too large
            while (outputStream.size() > MAX_IMAGE_SIZE_KB * 1024 && quality > MIN_QUALITY) {
                outputStream.reset();
                quality -= QUALITY_STEP;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            }

            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);

        } catch (Exception e) {
            Log.e(TAG, "Image compression failed", e);
            return null;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    private String encodeImageToBase64(Uri imageUri) {
        Bitmap bitmap = null;
        try (InputStream input = requireContext().getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeStream(input, null, options);

            if (bitmap == null) {
                Log.e(TAG, "Bitmap decoding failed");
                return null;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);

        } catch (Exception e) {
            Log.e(TAG, "Image encoding failed", e);
            return null;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    private Map<String, Object> createProductData(String base64Image) {
        Map<String, Object> product = new HashMap<>();
        product.put("productName", productNameEditText.getText().toString().trim());
        product.put("priceKsh", priceKshEditText.getText().toString().trim());
        product.put("priceUsd", priceUsdEditText.getText() != null ?
                priceUsdEditText.getText().toString() : "");
        product.put("discount", discountEditText.getText() != null ?
                discountEditText.getText().toString() : "");
        product.put("discountType", discountTypeSpinner.getText() != null ?
                discountTypeSpinner.getText().toString() : "");
        product.put("description", productDescriptionEditText.getText().toString().trim());
        product.put("category", categoryDropdown.getText().toString().trim());
        product.put("imageBase64", base64Image);
        product.put("timestamp", System.currentTimeMillis());
        return product;
    }

    private void saveToFirestore(Map<String, Object> product) {
        Log.d(TAG, "Saving product: " + product.toString());

        firestore.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Product saved successfully");
                    dismissProgressDialog();
                    showSuccessDialog();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore save failed", e);
                    dismissProgressDialog();
                    showToast("Failed to save: " + e.getMessage());
                });
    }

    private void showSuccessDialog() {
        if (!isAdded() || getActivity() == null || isRemoving()) return;

        Context context = getContext();
        if (context == null) context = requireActivity().getApplicationContext();

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Success");
            builder.setMessage("Product saved successfully!");
            builder.setPositiveButton("OK", (dialog, which) -> navigateToHome());
            builder.setCancelable(false);

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Success dialog failed", e);
            showToast("Product saved successfully!");
            navigateToHome();
        }
    }

    private void showProgressDialog(String message) {
        if (!isAdded()) return;

        mainHandler.post(() -> {
            try {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(requireContext());
                    progressDialog.setMessage(message);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                } else {
                    progressDialog.setMessage(message);
                    if (!progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Progress dialog error", e);
            }
        });
    }

    private void dismissProgressDialog() {
        if (!isAdded()) return;

        mainHandler.post(() -> {
            try {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error dismissing dialog", e);
            }
            progressDialog = null;
        });
    }

    private boolean validateInputs() {
        if (selectedImageUri == null) {
            showToast("Please upload a product image");
            return false;
        }

        String productName = productNameEditText.getText() != null ?
                productNameEditText.getText().toString().trim() : "";
        String priceKsh = priceKshEditText.getText() != null ?
                priceKshEditText.getText().toString().trim() : "";
        String description = productDescriptionEditText.getText() != null ?
                productDescriptionEditText.getText().toString().trim() : "";
        String category = categoryDropdown.getText() != null ?
                categoryDropdown.getText().toString().trim() : "";

        if (productName.isEmpty() || priceKsh.isEmpty() || description.isEmpty() || category.isEmpty()) {
            showToast("Please fill all required fields");
            return false;
        }
        return true;
    }

    private void clearForm() {
        mainHandler.post(() -> {
            if (!isAdded()) return;

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

    private void navigateToHome() {
        if (isAdded()) {
            try {
                NavHostFragment.findNavController(this).navigate(R.id.nav_home);
            } catch (Exception e) {
                Log.e(TAG, "Navigation failed", e);
            }
        }
    }

    private void showToast(String message) {
        mainHandler.post(() -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        dismissProgressDialog();
        productImageView.setImageDrawable(null);
        mainHandler.removeCallbacksAndMessages(null);
    }
}