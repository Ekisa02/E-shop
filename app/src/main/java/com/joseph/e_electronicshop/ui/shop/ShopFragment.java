package com.joseph.e_electronicshop.ui.shop;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.databinding.FragmentShopBinding;
import java.util.HashMap;
import java.util.Map;

public class ShopFragment extends Fragment {
    private FragmentShopBinding binding;
    private FirebaseFirestore db;
    private String productId;
    private String paymentMethod = "";
    private boolean paymentConfirmed = false;
    private boolean deliveryConfirmed = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // Get product details from arguments
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            String productName = getArguments().getString("productName");
            String productPrice = getArguments().getString("productPrice");
            String productImage = getArguments().getString("productImage");
            String description = getArguments().getString("productDescription");

            // Display product info
            binding.productName.setText(productName);
            binding.productPrice.setText("Ksh " + productPrice);
            binding.productDescription.setText(description);

            // Load image if available
            if (productImage != null && !productImage.isEmpty()) {
                byte[] imageBytes = android.util.Base64.decode(productImage, android.util.Base64.DEFAULT);
                Glide.with(requireContext())
                        .load(imageBytes)
                        .into(binding.productImage);
            }
        }

        setupPaymentOptions();
        setupConfirmationButtons();
    }

    private void setupPaymentOptions() {
        binding.paymentBeforeDelivery.setOnClickListener(v -> {
            paymentMethod = "before_delivery";
            binding.paymentBeforeDelivery.setStrokeColor(
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            );
            binding.paymentAfterDelivery.setStrokeColor(
                    ContextCompat.getColor(requireContext(), R.color.primary_text)
            );
            showPaymentGateway();
        });

        binding.paymentAfterDelivery.setOnClickListener(v -> {
            paymentMethod = "after_delivery";
            binding.paymentAfterDelivery.setStrokeColor(
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            );
            binding.paymentBeforeDelivery.setStrokeColor(
                    ContextCompat.getColor(requireContext(), R.color.primary_text)
            );
            showDeliveryTracking();
        });
    }

    private void showPaymentGateway() {
        binding.paymentGateway.setVisibility(View.VISIBLE);
        binding.trackingLayout.setVisibility(View.GONE);

        // Implement your payment gateway integration here
        binding.payNowButton.setOnClickListener(v -> {
            processPayment();
        });
    }

    private void showDeliveryTracking() {
        binding.paymentGateway.setVisibility(View.GONE);
        binding.trackingLayout.setVisibility(View.VISIBLE);

        // Simulate delivery tracking
        binding.trackDeliveryButton.setOnClickListener(v -> {
            simulateDeliveryProcess();
        });
    }

    private void processPayment() {
        // Implement your actual payment processing here
        // This is just a simulation

        binding.paymentProgress.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            binding.paymentProgress.setVisibility(View.GONE);
            paymentConfirmed = true;
            binding.paymentStatus.setText("Payment Confirmed ✓");
            binding.paymentStatus.setTextColor(getResources().getColor(R.color.green));
            showDeliveryTracking();
            updateOrderStatus("payment_confirmed");
        }, 2000);
    }

    private void simulateDeliveryProcess() {
        binding.deliveryProgress.setVisibility(View.VISIBLE);
        binding.deliverySteps.setText("1. Order confirmed\n2. Preparing for shipment");

        new Handler().postDelayed(() -> {
            binding.deliverySteps.append("\n3. Shipped - ETA 2 days");
        }, 3000);

        new Handler().postDelayed(() -> {
            binding.deliverySteps.append("\n4. Out for delivery");
            binding.confirmDeliveryButton.setVisibility(View.VISIBLE);
        }, 6000);

        binding.confirmDeliveryButton.setOnClickListener(v -> {
            deliveryConfirmed = true;
            binding.deliveryStatus.setText("Delivery Confirmed ✓");
            binding.deliveryStatus.setTextColor(getResources().getColor(R.color.green));

            if (paymentMethod.equals("after_delivery")) {
                binding.completePaymentButton.setVisibility(View.VISIBLE);
                binding.completePaymentButton.setOnClickListener(v2 -> processPayment());
            } else {
                binding.completeOrderButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupConfirmationButtons() {
        binding.completeOrderButton.setOnClickListener(v -> {
            updateOrderStatus("completed");
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.action_shopFragment_to_orderCompleteFragment);
        });
    }

    private void updateOrderStatus(String status) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("status", status);
        orderData.put("paymentMethod", paymentMethod);
        orderData.put("paymentConfirmed", paymentConfirmed);
        orderData.put("deliveryConfirmed", deliveryConfirmed);

        db.collection("orders")
                .document(productId)
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    // Remove from cart if order is completed
                    if (status.equals("completed")) {
                        db.collection("products")
                                .document(productId)
                                .update("inCart", false, "cartTimestamp", 0);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}