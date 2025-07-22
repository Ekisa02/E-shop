package com.joseph.e_electronicshop.ui.home;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.joseph.e_electronicshop.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminpageFragment extends Fragment {

    private ImageView productImageView;
    private Button uploadPhotoButton;
    private ProgressBar imageUploadProgressBar; // ProgressBar to show during image "upload"
    private Uri imageUri;

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_adminpage, container, false);

        productImageView = view.findViewById(R.id.productImageView);
        uploadPhotoButton = view.findViewById(R.id.uploadPhotoButton);
        imageUploadProgressBar = view.findViewById(R.id.imageUploadProgressBar);
        Button saveButton = view.findViewById(R.id.saveProductButton);
        EditText nameInput = view.findViewById(R.id.productNameEditText);
        EditText kshInput = view.findViewById(R.id.priceKshEditText);
        EditText usdInput = view.findViewById(R.id.priceUsdEditText);
        EditText discountInput = view.findViewById(R.id.discountEditText);
        AutoCompleteTextView discountType = view.findViewById(R.id.discountTypeSpinner);
        EditText descriptionInput = view.findViewById(R.id.productDescriptionEditText);
        ImageView productImage = view.findViewById(R.id.productImageView);



        saveButton.setOnClickListener(v -> {

            // Validate image
            if (!(productImageView.getDrawable() instanceof BitmapDrawable)) {
                Toast.makeText(getContext(), "Please select a product image first.", Toast.LENGTH_SHORT).show();
                return;
            }

            BitmapDrawable drawable = (BitmapDrawable) productImageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            if (bitmap == null) {
                Toast.makeText(getContext(), "Image not found. Try re-uploading.", Toast.LENGTH_SHORT).show();
                return;
            }

            imageUploadProgressBar.setVisibility(View.VISIBLE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference()
                    .child("products")
                    .child(UUID.randomUUID().toString() + ".jpg");

            UploadTask uploadTask = storageRef.putBytes(data);

            uploadTask.addOnSuccessListener(taskSnapshot ->
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String uploadedImageUrl = uri.toString();

                        // 3. Save to Firestore
                        Map<String, Object> product = new HashMap<>();
                        product.put("name", nameInput.getText().toString());
                        product.put("price_ksh", Double.parseDouble(kshInput.getText().toString()));
                        product.put("price_usd", Double.parseDouble(usdInput.getText().toString()));
                        product.put("discount", Double.parseDouble(discountInput.getText().toString()));
                        product.put("discount_type", discountType.getText().toString());
                        product.put("description", descriptionInput.getText().toString());
                        product.put("photo_url", uploadedImageUrl);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("products")
                                .add(product)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getContext(), "Product saved!", Toast.LENGTH_SHORT).show();
                                    imageUploadProgressBar.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    imageUploadProgressBar.setVisibility(View.GONE);
                                });

                    })
            ).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                imageUploadProgressBar.setVisibility(View.GONE);
            });
        });


        uploadPhotoButton.setOnClickListener(v -> showImagePickerDialog());

        productImageView.setOnClickListener(v -> showImagePickerDialog()); // Allow re-select/edit image

        initActivityResultLaunchers();

        AutoCompleteTextView categoryDropdown = view.findViewById(R.id.categoryDropdown);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );

        categoryDropdown.setAdapter(adapter);



        return view;
    }

    private void initActivityResultLaunchers() {
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        simulateUploadAndDisplay(selectedImage);
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && imageUri != null) {
                        simulateUploadAndDisplay(imageUri);
                    }
                });
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        } else {
                            openCamera();
                        }
                    } else {
                        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickIntent.setType("image/*");
                        imagePickerLauncher.launch(pickIntent);
                    }
                })
                .show();
    }

    private void openCamera() {
        File photoFile = new File(requireContext().getExternalCacheDir(), "camera_photo.jpg");
        imageUri = FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName() + ".fileprovider", photoFile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraLauncher.launch(cameraIntent);
    }

    /**
     * Simulates upload progress, compresses the image, and sets it in the ImageView
     */
    private void simulateUploadAndDisplay(Uri uri) {
        imageUploadProgressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            imageUploadProgressBar.setVisibility(View.GONE);

            try (InputStream stream = requireActivity().getContentResolver().openInputStream(uri)) {
                Bitmap originalBitmap = BitmapFactory.decodeStream(stream);
                if (originalBitmap != null) {
                    // Compress image before displaying
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 800, 800, true);
                    productImageView.setImageBitmap(resizedBitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }, 1500); // Simulate 1.5 sec delay
    }

    // Optional: Restore imageUri on rotation
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageUri != null) {
            outState.putString("image_uri", imageUri.toString());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("image_uri")) {
            imageUri = Uri.parse(savedInstanceState.getString("image_uri"));
            simulateUploadAndDisplay(imageUri);
        }
    }


    //category list
    private final String[] categories = new String[]{
            "Laptops", "Smartphones", "Tablets", "Televisions", "Headphones",
            "Cameras", "Smartwatches", "Gaming Consoles", "Speakers", "Drones",
            "Monitors", "Printers", "Hard Drives", "Power Banks", "Routers"
    };

}
