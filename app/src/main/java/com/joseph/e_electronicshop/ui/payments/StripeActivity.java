package com.joseph.e_electronicshop.ui.payments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.ui.payments.OrderTrackingActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class StripeActivity extends AppCompatActivity {

    // Currency Configuration
    private static final double KSH_TO_USD_RATE = 0.0078; // 1 KSH = 0.0078 USD
    private static final String CURRENCY_KSH = "Ksh ";
    private static final String CURRENCY_USDC = "USDC";
    private static final String CURRENCY_USD_PREFIX = "≈ $";

    // UI Components
    private EditText etAmountInput;
    private TextView tvAmountKsh, tvAmountUsdc, tvExchangeRate;
    private Button btnPay;

    // Animation and Feedback
    private ProgressDialog progressDialog;
    private boolean isProcessingPayment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe);
        setupViews();
        setupCurrencyInput();
        setupPaymentButton();
    }

    private void setupViews() {
        etAmountInput = findViewById(R.id.etAmountInput);
        tvAmountKsh = findViewById(R.id.tvAmountKsh);
        tvAmountUsdc = findViewById(R.id.tvAmountUsdc);
        tvExchangeRate = findViewById(R.id.tvExchangeRate);
        btnPay = findViewById(R.id.btnPay);

        // Set initial exchange rate display
        updateExchangeRateDisplay();
    }

    private void updateExchangeRateDisplay() {
        String rateInfo = String.format(Locale.getDefault(),
                "1 %s = $%.4f", CURRENCY_USDC, 1/KSH_TO_USD_RATE);
        tvExchangeRate.setText(rateInfo);
    }

    private void setupCurrencyInput() {
        etAmountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isProcessingPayment) return;

                try {
                    String input = s.toString();
                    double amount = input.isEmpty() ? 0 : Double.parseDouble(input);
                    updateCurrencyDisplay(amount);

                    // Auto-format input with thousands separators
                    if (!input.isEmpty() && !input.equals(".")) {
                        String formatted = formatCurrency(amount, "");
                        if (!input.equals(formatted)) {
                            etAmountInput.removeTextChangedListener(this);
                            etAmountInput.setText(formatted);
                            etAmountInput.setSelection(formatted.length());
                            etAmountInput.addTextChangedListener(this);
                        }
                    }
                } catch (NumberFormatException e) {
                    resetCurrencyDisplay();
                }
            }
        });
    }

    private void updateCurrencyDisplay(double kshAmount) {
        // KSH Display
        tvAmountKsh.setText(formatCurrency(kshAmount, CURRENCY_KSH));

        // USDC Conversion
        double usdAmount = convertKshToUsd(kshAmount);
        tvAmountUsdc.setText(formatCurrency(usdAmount, CURRENCY_USD_PREFIX) + " " + CURRENCY_USDC);

        // Visual feedback
        if (kshAmount > 0) {
            tvAmountKsh.setTextColor(ContextCompat.getColor(this, R.color.stripe_purple));
            tvAmountUsdc.setTextColor(ContextCompat.getColor(this, R.color.medium_text));
        } else {
            resetCurrencyDisplay();
        }
    }

    private void resetCurrencyDisplay() {
        tvAmountKsh.setText(formatCurrency(0, CURRENCY_KSH));
        tvAmountUsdc.setText(formatCurrency(0, CURRENCY_USD_PREFIX) + " " + CURRENCY_USDC);
        tvAmountKsh.setTextColor(ContextCompat.getColor(this, R.color.light_gray));
        tvAmountUsdc.setTextColor(ContextCompat.getColor(this, R.color.light_gray));
    }

    private double convertKshToUsd(double ksh) {
        return ksh * KSH_TO_USD_RATE;
    }

    private String formatCurrency(double amount, String prefix) {
        DecimalFormat formatter = new DecimalFormat("#,###.##",
                DecimalFormatSymbols.getInstance(Locale.US));
        return prefix + formatter.format(amount);
    }

    private void setupPaymentButton() {
        btnPay.setOnClickListener(v -> {
            if (isProcessingPayment) return;

            String rawInput = Objects.requireNonNull(etAmountInput.getText()).toString();
            if (rawInput.isEmpty()) {
                showInputError("Please enter payment amount");
                return;
            }

            try {
                double amount = Double.parseDouble(rawInput.replace(",", ""));
                if (amount <= 0) {
                    showInputError("Amount must be greater than 0");
                    return;
                }

                showPaymentConfirmation(amount);
            } catch (NumberFormatException e) {
                showInputError("Invalid amount format");
            }
        });
    }

    private void showInputError(String message) {
        etAmountInput.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        etAmountInput.setError(message);
    }

    private void showPaymentConfirmation(final double kshAmount) {
        double usdAmount = convertKshToUsd(kshAmount);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Payment")
                .setMessage(String.format(Locale.getDefault(),
                        "You are about to pay:\n\n%s\n%s\n\nExchange Rate: 1 %s = $%.4f",
                        formatCurrency(kshAmount, CURRENCY_KSH),
                        formatCurrency(usdAmount, CURRENCY_USD_PREFIX) + " " + CURRENCY_USDC,
                        CURRENCY_USDC, 1/KSH_TO_USD_RATE))
                .setPositiveButton("Pay Now", (dialog, which) -> processPayment(kshAmount))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processPayment(final double kshAmount) {
        isProcessingPayment = true;
        showProgressDialog("Processing " + formatCurrency(kshAmount, CURRENCY_KSH) + "...");

        // Simulate network request
        new Handler().postDelayed(() -> {
            dismissProgressDialog();
            isProcessingPayment = false;

            String transactionId = generateTransactionId();
            String timestamp = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(new Date());

            showPaymentSuccess(kshAmount, transactionId, timestamp);
        }, 3000);
    }

    private String generateTransactionId() {
        return "ch_" + System.currentTimeMillis() + "_" + (1000 + new Random().nextInt(9000));
    }

    private void showPaymentSuccess(double kshAmount, String transactionId, String timestamp) {
        double usdAmount = convertKshToUsd(kshAmount);

        new AlertDialog.Builder(this)
                .setTitle("✅ Payment Successful")
                .setMessage(String.format(Locale.getDefault(),
                        "Paid: %s\nConverted: %s\n\nTransaction ID: %s\nDate: %s\n\nThank you for your purchase!",
                        formatCurrency(kshAmount, CURRENCY_KSH),
                        formatCurrency(usdAmount, CURRENCY_USD_PREFIX) + " " + CURRENCY_USDC,
                        transactionId,
                        timestamp))
                .setPositiveButton("Track Order", (dialog, which) -> navigateToOrderTracking())
                .setCancelable(false)
                .show();
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void navigateToOrderTracking() {
        startActivity(new Intent(this, OrderTrackingActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }
}