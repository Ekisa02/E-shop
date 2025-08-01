package com.joseph.e_electronicshop.ui.payments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.ui.payments.OrderTrackingActivity;
import com.joseph.e_electronicshop.ui.viewmodels.SharedProductViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MpesaActivity extends AppCompatActivity {

    private static final String PAYBILL_NUMBER = "247247";
    private static final String ACCOUNT_NUMBER = "105030186219";
    private static final String MPESA_SHORTCODE = "MPESA";

    private TextView tvAmount, tvPaybillInfo;
    private Button btnAutoPay, btnManualPay;
    private TextInputEditText etMpesaRef;
    private double amount;
    private ProgressDialog progressDialog;
    private SharedProductViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpesa);

        initializeViews();
        setupViewModel();
        setupClickListeners();
    }

    private void initializeViews() {
        tvAmount = findViewById(R.id.tvAmount);
        tvPaybillInfo = findViewById(R.id.tvPaybillInfo);
        btnAutoPay = findViewById(R.id.btnAutoPay);
        btnManualPay = findViewById(R.id.btnManualPay);
        etMpesaRef = findViewById(R.id.etMpesaRef);
    }

    private void setupViewModel() {
        sharedViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(SharedProductViewModel.class);

        sharedViewModel.getProduct().observe(this, product -> {
            if (product != null) {
                amount = Double.parseDouble(product.getPriceKsh());
                tvAmount.setText(String.format(Locale.getDefault(), "Ksh %.2f", amount));
                tvPaybillInfo.setText(String.format("Paybill: %s\nAccount: %s",
                        PAYBILL_NUMBER, ACCOUNT_NUMBER));
            } else {
                Toast.makeText(this, "Product information not available", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updatePaymentUI() {
        tvAmount.setText(String.format(Locale.getDefault(), "Ksh %.2f", amount));
        tvPaybillInfo.setText(String.format("Paybill: %s\nAccount: %s", PAYBILL_NUMBER, ACCOUNT_NUMBER));
    }

    private void setupClickListeners() {
        btnAutoPay.setOnClickListener(v -> initiateAutoPayment());
        btnManualPay.setOnClickListener(v -> validateAndProcessManualPayment());
    }

    private void initiateAutoPayment() {
        showProgressDialog("Initiating MPesa payment...");

        new Handler().postDelayed(() -> {
            dismissProgressDialog();
            simulateMpesaUssdDial();
        }, 1500);
    }

    private void simulateMpesaUssdDial() {
        new AlertDialog.Builder(this)
                .setTitle("MPesa Payment")
                .setMessage(String.format(Locale.getDefault(),
                        "Dial *234*1*%s*%.2f# to complete payment",
                        PAYBILL_NUMBER, amount))
                .setPositiveButton("OK", (dialog, which) -> {
                    simulateMpesaConfirmation();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void simulateMpesaConfirmation() {
        showProgressDialog("Processing payment with MPesa...");

        new Handler().postDelayed(() -> {
            dismissProgressDialog();

            // Generate random transaction details
            String transactionCode = "MPS" + new Random().nextInt(900000) + 100000;
            String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            // Simulate receiving confirmation SMS
            simulateMpesaSMS(transactionCode, timestamp);

            new AlertDialog.Builder(this)
                    .setTitle("Payment Successful")
                    .setMessage(String.format(Locale.getDefault(),
                            "You've sent Ksh %.2f to %s\n\nTransaction: %s\nDate: %s",
                            amount, ACCOUNT_NUMBER, transactionCode, timestamp))
                    .setPositiveButton("OK", (dialog, which) -> navigateToOrderTracking())
                    .setCancelable(false)
                    .show();
        }, 3000);
    }

    private void simulateMpesaSMS(String transactionCode, String timestamp) {
        try {
            String smsMessage = String.format(Locale.getDefault(),
                    "Confirmed. Ksh%.2f sent to %s for account %s on %s. Transaction ID: %s. New M-PESA balance is Ksh%.2f",
                    amount, PAYBILL_NUMBER, ACCOUNT_NUMBER, timestamp, transactionCode,
                    (new Random().nextInt(9000) + 1000 + amount/10));

            Toast.makeText(this, "MPesa confirmation SMS received", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validateAndProcessManualPayment() {
        String reference = etMpesaRef.getText().toString().trim();

        if (reference.isEmpty()) {
            etMpesaRef.setError("Please enter MPesa reference");
            return;
        }

        if (reference.length() < 6) {
            etMpesaRef.setError("Reference should be at least 6 characters");
            return;
        }

        showPaymentConfirmationDialog(reference);
    }

    private void showPaymentConfirmationDialog(String reference) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Payment")
                .setMessage(String.format(Locale.getDefault(),
                        "Please confirm you've paid Ksh %.2f to:\n\nPaybill: %s\nAccount: %s\n\nReference: %s",
                        amount, PAYBILL_NUMBER, ACCOUNT_NUMBER, reference))
                .setPositiveButton("I've Paid", (dialog, which) -> processManualPayment(reference))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processManualPayment(String reference) {
        showProgressDialog("Verifying payment...");

        new Handler().postDelayed(() -> {
            dismissProgressDialog();

            String transactionCode = "MPM" + new Random().nextInt(900000) + 100000;
            String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            new AlertDialog.Builder(this)
                    .setTitle("Payment Verified")
                    .setMessage(String.format(Locale.getDefault(),
                            "Payment of Ksh %.2f verified successfully\n\nTransaction: %s\nDate: %s\nReference: %s",
                            amount, transactionCode, timestamp, reference))
                    .setPositiveButton("OK", (dialog, which) -> navigateToOrderTracking())
                    .setCancelable(false)
                    .show();
        }, 2500);
    }

    private void showProgressDialog(String message) {
        dismissProgressDialog();
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