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

import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.ui.payments.OrderTrackingActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class PaypalActivity extends AppCompatActivity {

    private TextView tvAmount, tvKshAmount, tvUsdcAmount, tvExchangeRate;
    private EditText etEmail, etPassword, etAmount;
    private Button btnPay;
    private double amount;
    private ProgressDialog progressDialog;

    // Exchange rates (these could be fetched from an API in a real app)
    private static final double USD_TO_KSH_RATE = 150.50;
    private static final double USD_TO_USDC_RATE = 1.0; // Assuming 1:1 for simplicity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal);

        // Get amount from intent
        amount = getIntent().getDoubleExtra("amount", 0.0);

        initializeViews();
        setupPaymentButton();
        setupTextWatchers();
        updateCurrencyConversions(amount);
    }

    private void initializeViews() {
        tvAmount = findViewById(R.id.tvAmount);
        tvKshAmount = findViewById(R.id.tvKshAmount);
        tvUsdcAmount = findViewById(R.id.tvUsdcAmount);
        tvExchangeRate = findViewById(R.id.tvExchangeRate);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAmount = findViewById(R.id.etAmount);
        btnPay = findViewById(R.id.btnPay);

        // Set initial amount with proper formatting
        updateDisplayAmount(amount);

        // Set exchange rate text
        tvExchangeRate.setText(String.format(Locale.getDefault(),
                "Exchange rate: 1 USD = %.2f KSH | 1 USD = %.2f USDC",
                USD_TO_KSH_RATE, USD_TO_USDC_RATE));

        // Setup amount edit text listener
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!s.toString().isEmpty()) {
                        amount = Double.parseDouble(s.toString());
                        updateDisplayAmount(amount);
                        updateCurrencyConversions(amount);
                    }
                } catch (NumberFormatException e) {
                    // Handle invalid number format
                }
            }
        });
    }

    private void updateDisplayAmount(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        tvAmount.setText(String.format("$%s", df.format(amount)));
    }

    private void updateCurrencyConversions(double usdAmount) {
        double kshAmount = usdAmount * USD_TO_KSH_RATE;
        double usdcAmount = usdAmount * USD_TO_USDC_RATE;

        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));

        tvKshAmount.setText(String.format("KSH %s", df.format(kshAmount)));
        tvUsdcAmount.setText(String.format("USDC %s", df.format(usdcAmount)));
    }

    private void setupTextWatchers() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        };

        etEmail.addTextChangedListener(validationWatcher);
        etPassword.addTextChangedListener(validationWatcher);
    }

    private void validateInputs() {
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        btnPay.setEnabled(!email.isEmpty() && !password.isEmpty());
    }

    private void setupPaymentButton() {
        btnPay.setOnClickListener(v -> {
            if (!validateForm()) {
                return;
            }

            showPaymentConfirmation();
        });
    }

    private boolean validateForm() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
            etEmail.setError("Email required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
            etEmail.setError("Invalid email format");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
            etPassword.setError("Password required");
            return false;
        }

        return true;
    }

    private void showPaymentConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm PayPal Payment")
                .setMessage(String.format(Locale.getDefault(),
                        "You are about to pay $%.2f from %s\n\n" +
                                "Equivalent to:\n" +
                                "KSH %.2f\n" +
                                "USDC %.2f\n\n" +
                                "Continue?",
                        amount, etEmail.getText().toString(),
                        amount * USD_TO_KSH_RATE,
                        amount * USD_TO_USDC_RATE))
                .setPositiveButton("Pay Now", (dialog, which) -> processPayment())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processPayment() {
        showProgressDialog("Connecting to PayPal...");

        // Simulate API call
        new Handler().postDelayed(() -> {
            dismissProgressDialog();

            // Generate transaction details
            String transactionId = "PAY-" + System.currentTimeMillis();
            String timestamp = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    .format(new Date());

            showPaymentSuccess(transactionId, timestamp);
        }, 3000);
    }

    private void showPaymentSuccess(String transactionId, String timestamp) {
        new AlertDialog.Builder(this)
                .setTitle("Payment Successful")
                .setMessage(String.format(Locale.getDefault(),
                        "Paid: $%.2f\n" +
                                "KSH %.2f\n" +
                                "USDC %.2f\n\n" +
                                "Transaction ID: %s\n" +
                                "Date: %s\n\n" +
                                "A receipt has been sent to your email",
                        amount,
                        amount * USD_TO_KSH_RATE,
                        amount * USD_TO_USDC_RATE,
                        transactionId, timestamp))
                .setPositiveButton("View Order", (dialog, which) -> navigateToOrderTracking())
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