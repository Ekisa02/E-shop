package com.joseph.e_electronicshop.ui.shop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.databinding.FragmentShopBinding;
import com.joseph.e_electronicshop.ui.payments.MpesaActivity;
import com.joseph.e_electronicshop.ui.payments.OrderTrackingActivity;
import com.joseph.e_electronicshop.ui.payments.PaypalActivity;
import com.joseph.e_electronicshop.ui.payments.StripeActivity;
import com.joseph.e_electronicshop.ui.viewmodels.SharedProductViewModel;

public class ShopFragment extends Fragment {

    private FragmentShopBinding binding;
    private Button purchaseButton , orderbtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        purchaseButton = view.findViewById(R.id.purchaseButton);
        purchaseButton.setVisibility(View.GONE); // Hidden by default

        orderbtn= view.findViewById(R.id.orderbtn);

        orderbtn.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), OrderTrackingActivity.class)));

        SharedProductViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedProductViewModel.class);

        viewModel.getProduct().observe(getViewLifecycleOwner(), product -> {
            if (product != null && binding != null) {
                binding.productName.setText(product.getProductName());
                binding.productPrice.setText("Ksh " + product.getPriceKsh());
                binding.productDescription.setText(product.getDescription());

                // Show button only if product is available
                if (product.isAvailable()) { // Make sure your Product model has this method
                    purchaseButton.setVisibility(View.VISIBLE);
                } else {
                    purchaseButton.setVisibility(View.GONE);
                }

                try {
                    byte[] decodedString = Base64.decode(product.getImageBase64(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    binding.productImage.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    Log.e("ShopFragment", "Image decode error", e);
                    Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (purchaseButton != null) {
            purchaseButton.setOnClickListener(v -> showPaymentDialog());
        }
    }

    private void showPaymentDialog() {
        try {
            // Create custom items
            String[] options = {"Mpesa", "PayPal", "Stripe"};
            int[] icons = {
                    R.drawable.ic_mpesa,
                    R.drawable.ic_paypal,
                    R.drawable.ic_stripe
            };

            // Custom adapter for icons
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    requireContext(),
                    R.layout.item_payment_option,
                    R.id.payment_option_text,
                    options
            ) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    try {
                        ImageView iconView = view.findViewById(R.id.payment_option_icon);
                        TextView textView = view.findViewById(R.id.payment_option_text);

                        iconView.setImageResource(icons[position]);
                        switch (position) {
                            case 0: // Mpesa
                                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.mpesa_green));
                                break;
                            case 1: // PayPal
                                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.paypal_blue));
                                break;
                            case 2: // Stripe
                                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.stripe_purple));
                                break;
                        }
                    } catch (Exception e) {
                        Log.e("PaymentAdapter", "Error creating view", e);
                    }
                    return view;
                }
            };

            // Button press animation
            if (getView() != null) {
                getView().animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                        .withEndAction(() -> getView().animate().scaleX(1f).scaleY(1f));
            }

            // Vibrate if available
            try {
                Vibrator vibrator = (Vibrator) requireContext().getSystemService(requireContext().VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(15);
                    }
                }
            } catch (Exception e) {
                Log.e("Vibration", "Error with vibration", e);
            }

            // Create dialog with try-catch
            try {
                new AlertDialog.Builder(requireContext(), R.style.PaymentDialogTheme)
                        .setTitle("Choose Payment Method")
                        .setIcon(R.drawable.ic_payment)
                        .setAdapter(adapter, (dialog, which) -> {
                            try {
                                Intent intent = null;
                                switch (which) {
                                    case 0:
                                        intent = new Intent(requireContext(), MpesaActivity.class);
                                        break;
                                    case 1:
                                        intent = new Intent(requireContext(), PaypalActivity.class);
                                        break;
                                    case 2:
                                        intent = new Intent(requireContext(), StripeActivity.class);
                                        break;
                                }
                                if (intent != null && isAdded()) {
                                    startActivity(intent);
                                    requireActivity().overridePendingTransition(
                                            R.anim.slide_in_right,
                                            R.anim.slide_out_left
                                    );
                                }
                            } catch (Exception e) {
                                Log.e("PaymentSelection", "Error starting payment", e);
                                Toast.makeText(requireContext(), "Payment service unavailable", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } catch (Exception e) {
                Log.e("PaymentDialog", "Error showing dialog", e);
                Toast.makeText(requireContext(), "Cannot show payment options", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ShopFragment", "Error in payment flow", e);
            Toast.makeText(requireContext(), "Payment system error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}