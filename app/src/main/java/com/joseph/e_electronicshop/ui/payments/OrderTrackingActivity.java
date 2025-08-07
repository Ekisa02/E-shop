package com.joseph.e_electronicshop.ui.payments;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.ui.viewmodels.SharedProductViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class OrderTrackingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView statusText, estimatedDelivery, orderNumber, orderDate;
    private ImageView statusIcon1, statusIcon2, statusIcon3, statusIcon4;
    private View statusLine1, statusLine2, statusLine3;
    private Button refreshButton;
    private SharedProductViewModel viewModel;

    // Simulated order status (in a real app, this would come from your backend)
    private final String[] statuses = {
            "Order Placed",
            "Processing",
            "Shipped",
            "Out for Delivery",
            "Delivered"
    };

    private int currentStatus = 0;
    private final Handler handler = new Handler();
    private Runnable statusUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);
        estimatedDelivery = findViewById(R.id.estimatedDelivery);
        orderNumber = findViewById(R.id.orderNumber);
        orderDate = findViewById(R.id.orderDate);
        statusIcon1 = findViewById(R.id.statusIcon1);
        statusIcon2 = findViewById(R.id.statusIcon2);
        statusIcon3 = findViewById(R.id.statusIcon3);
        statusIcon4 = findViewById(R.id.statusIcon4);
        statusLine1 = findViewById(R.id.statusLine1);
        statusLine2 = findViewById(R.id.statusLine2);
        statusLine3 = findViewById(R.id.statusLine3);
        refreshButton = findViewById(R.id.refreshButton);

        // Generate random order details
        generateOrderDetails();

        // Start simulating order progress
        startOrderProgressSimulation();

        refreshButton.setOnClickListener(v -> {
            refreshButton.setEnabled(false);
            resetOrderProgress();
            startOrderProgressSimulation();
        });
    }

    private void generateOrderDetails() {
        // Generate random order number
        Random random = new Random();
        String orderNum = "ORD-" + (100000 + random.nextInt(900000));
        orderNumber.setText(orderNum);

        // Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        orderDate.setText(sdf.format(new Date()));

        // Set estimated delivery (2-5 days from now)
        int deliveryDays = 2 + random.nextInt(4);
        String deliveryDate = sdf.format(new Date(System.currentTimeMillis() + deliveryDays * 24 * 60 * 60 * 1000));
        estimatedDelivery.setText("Estimated delivery: " + deliveryDate);
    }

    private void startOrderProgressSimulation() {
        // Reset to initial state
        currentStatus = 0;
        updateStatusUI();

        // Simulate order progress updates
        statusUpdater = new Runnable() {
            @Override
            public void run() {
                if (currentStatus < statuses.length - 1) {
                    currentStatus++;
                    updateStatusUI();

                    // Schedule next update (faster at first, slower as it progresses)
                    int delay = currentStatus == 1 ? 3000 :
                            currentStatus == 2 ? 5000 :
                                    currentStatus == 3 ? 7000 : 0;

                    if (delay > 0) {
                        handler.postDelayed(this, delay);
                    } else {
                        refreshButton.setEnabled(true);
                    }
                }
            }
        };

        // Start with initial delay
        handler.postDelayed(statusUpdater, 2000);
    }

    private void resetOrderProgress() {
        handler.removeCallbacks(statusUpdater);
        progressBar.setProgress(0);
        statusText.setText(statuses[0]);

        // Reset all status indicators to inactive
        statusIcon1.setImageResource(R.drawable.ic_order_placed);
        statusIcon2.setImageResource(R.drawable.ic_order_processing);
        statusIcon3.setImageResource(R.drawable.ic_order_shipped);
        statusIcon4.setImageResource(R.drawable.ic_order_delivered);

        statusIcon1.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        statusIcon2.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        statusIcon3.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        statusIcon4.setColorFilter(ContextCompat.getColor(this, R.color.gray));

        statusLine1.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
        statusLine2.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
        statusLine3.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
    }

    private void updateStatusUI() {
        // Update progress bar
        int progress = (int) ((currentStatus / (float) (statuses.length - 1)) * 100);
        progressBar.setProgress(progress);

        // Update status text
        statusText.setText(statuses[currentStatus]);

        // Update status indicators
        switch (currentStatus) {
            case 1: // Processing
                statusIcon1.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
                statusLine1.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                statusIcon2.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
                break;
            case 2: // Shipped
                statusLine2.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                statusIcon3.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
                break;
            case 3: // Out for Delivery
                statusLine3.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                statusIcon4.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
                break;
            case 4: // Delivered
                statusIcon4.setColorFilter(ContextCompat.getColor(this, R.color.green));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(statusUpdater);
    }
}