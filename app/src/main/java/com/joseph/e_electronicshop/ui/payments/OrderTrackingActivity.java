package com.joseph.e_electronicshop.ui.payments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.joseph.e_electronicshop.R;
import com.joseph.e_electronicshop.ui.Adapters.Order;
import com.joseph.e_electronicshop.ui.Adapters.OrderStatus;
import com.joseph.e_electronicshop.ui.Adapters.Product;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderTrackingActivity extends AppCompatActivity {

    private static final String TAG = "OrderTrackingActivity";
    private Order currentOrder;
    private Handler trackingHandler = new Handler();
    private Runnable trackingRunnable;
    private int currentStatusIndex = 0;
    private List<OrderStatus> trackingStatuses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        // Get order data from intent with null checks
        if (getIntent() == null || getIntent().getSerializableExtra("order") == null) {
            Toast.makeText(this, "Order data not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            currentOrder = (Order) getIntent().getSerializableExtra("order");
            if (currentOrder == null) {
                Toast.makeText(this, "Invalid order data", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d(TAG, "Order received: " + currentOrder.getId());

            // Initialize tracking statuses
            initializeTrackingStatuses();

            // Setup UI
            setupOrderHeader();
            setupOrderItems();
            setupDeliveryInfo();
            setupButtons();

            // Start tracking simulation
            startTrackingSimulation();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing activity", e);
            Toast.makeText(this, "Error loading order details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeTrackingStatuses() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

        trackingStatuses.add(new OrderStatus(
                "Order Placed",
                sdf.format(now),
                "We've received your order",
                R.drawable.ic_order_placed,
                true
        ));

        trackingStatuses.add(new OrderStatus(
                "Order Confirmed",
                sdf.format(new Date(now.getTime() + 3600000)),
                "Seller has confirmed your order",
                R.drawable.ic_order_confirmed,
                false
        ));

        trackingStatuses.add(new OrderStatus(
                "Processing",
                sdf.format(new Date(now.getTime() + 7200000)),
                "Your item is being prepared",
                R.drawable.ic_processing,
                false
        ));

        trackingStatuses.add(new OrderStatus(
                "Shipped",
                sdf.format(new Date(now.getTime() + 10800000)),
                "Your item has been shipped",
                R.drawable.ic_shipped,
                false
        ));

        trackingStatuses.add(new OrderStatus(
                "Out for Delivery",
                sdf.format(new Date(now.getTime() + 14400000)),
                "Your item is with the delivery partner",
                R.drawable.ic_out_for_delivery,
                false
        ));

        trackingStatuses.add(new OrderStatus(
                "Delivered",
                sdf.format(new Date(now.getTime() + 18000000)),
                "Your item has been delivered",
                R.drawable.ic_delivered,
                false
        ));
    }

    private void setupOrderHeader() {
        try {
            TextView tvOrderId = findViewById(R.id.tvOrderId);
            TextView tvOrderStatus = findViewById(R.id.tvOrderStatus);
            TextView tvEstimatedDelivery = findViewById(R.id.tvEstimatedDelivery);

            tvOrderId.setText(String.format("Order #%s", currentOrder.getId()));
            tvOrderStatus.setText(currentOrder.getStatus() != null ? currentOrder.getStatus() : "Status unknown");

            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
            Date deliveryDate = new Date(System.currentTimeMillis() + 259200000L); // 3 days later
            tvEstimatedDelivery.setText(String.format("Estimated delivery: %s", sdf.format(deliveryDate)));
        } catch (Exception e) {
            Log.e(TAG, "Error setting up order header", e);
        }
    }

    private void setupOrderItems() {
        LinearLayout itemsContainer = findViewById(R.id.orderItemsContainer);
        itemsContainer.removeAllViews();

        if (currentOrder.getItems() == null || currentOrder.getItems().isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No items in this order");
            emptyView.setTextSize(16);
            emptyView.setGravity(Gravity.CENTER);
            itemsContainer.addView(emptyView);
            return;
        }

        DecimalFormat priceFormat = new DecimalFormat("KSh #,##0.00");

        for (Product product : currentOrder.getItems()) {
            if (product == null) continue;

            try {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_order_product, itemsContainer, false);
                ImageView ivProduct = itemView.findViewById(R.id.ivProductImage);
                TextView tvName = itemView.findViewById(R.id.tvProductName);
                TextView tvPrice = itemView.findViewById(R.id.tvProductPrice);
                TextView tvQuantity = itemView.findViewById(R.id.tvProductQuantity);

                // Load product image
                if (product.getImageBase64() != null && !product.getImageBase64().isEmpty()) {
                    try {
                        byte[] decodedString = Base64.decode(product.getImageBase64(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        ivProduct.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        ivProduct.setImageResource(R.drawable.ic_product_placeholder);
                    }
                } else {
                    ivProduct.setImageResource(R.drawable.ic_product_placeholder);
                }

                tvName.setText(product.getProductName() != null ? product.getProductName() : "Unknown Product");

                try {
                    tvPrice.setText(priceFormat.format(Double.parseDouble(
                            product.getPriceKsh() != null ? product.getPriceKsh() : "0"
                    )));
                } catch (NumberFormatException e) {
                    tvPrice.setText("KSh 0.00");
                }

                tvQuantity.setText("Qty: 1");
                itemsContainer.addView(itemView);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up order item", e);
            }
        }

        // Set order totals
        try {
            TextView tvSubtotal = findViewById(R.id.tvSubtotal);
            TextView tvShipping = findViewById(R.id.tvShipping);
            TextView tvTax = findViewById(R.id.tvTax);
            TextView tvTotal = findViewById(R.id.tvTotal);

            tvSubtotal.setText(priceFormat.format(currentOrder.getSubtotal()));
            tvShipping.setText(priceFormat.format(currentOrder.getShipping()));
            tvTax.setText(priceFormat.format(currentOrder.getTax()));
            tvTotal.setText(priceFormat.format(currentOrder.getTotal()));
        } catch (Exception e) {
            Log.e(TAG, "Error setting order totals", e);
        }
    }

    private void setupDeliveryInfo() {
        try {
            TextView tvAddress = findViewById(R.id.tvDeliveryAddress);
            TextView tvContact = findViewById(R.id.tvContactNumber);

            tvAddress.setText(currentOrder.getShippingAddress() != null ?
                    currentOrder.getShippingAddress() : "Address not specified");

            tvContact.setText(currentOrder.getContactNumber() != null ?
                    currentOrder.getContactNumber() : "Contact not available");
        } catch (Exception e) {
            Log.e(TAG, "Error setting delivery info", e);
        }
    }

    private void setupButtons() {
        try {
            MaterialButton btnTrackOnMap = findViewById(R.id.btnTrackOnMap);
            MaterialButton btnContactSupport = findViewById(R.id.btnContactSupport);

            btnTrackOnMap.setOnClickListener(v -> {
                try {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" +
                            (currentOrder.getShippingAddress() != null ?
                                    currentOrder.getShippingAddress() : ""));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Could not open map", Toast.LENGTH_SHORT).show();
                }
            });

            btnContactSupport.setOnClickListener(v -> {
                try {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:support@eelectronicshop.com"));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                            "Support for Order #" + currentOrder.getId());
                    startActivity(Intent.createChooser(emailIntent, "Contact Support"));
                } catch (Exception e) {
                    Toast.makeText(this, "Could not open email", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up buttons", e);
        }
    }

    private void startTrackingSimulation() {
        if (trackingStatuses == null || trackingStatuses.isEmpty()) {
            return;
        }

        trackingRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (currentStatusIndex < trackingStatuses.size()) {
                        OrderStatus status = trackingStatuses.get(currentStatusIndex);
                        if (status != null) {
                            updateTrackingStatus(status);
                        }
                        currentStatusIndex++;
                        trackingHandler.postDelayed(this, 3000);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in tracking simulation", e);
                }
            }
        };
        trackingHandler.postDelayed(trackingRunnable, 1000);
    }

    private void updateTrackingStatus(OrderStatus status) {
        try {
            LinearLayout timeline = findViewById(R.id.trackingTimeline);
            View statusView = LayoutInflater.from(this).inflate(R.layout.item_tracking_step, timeline, false);

            ImageView ivIcon = statusView.findViewById(R.id.ivStatusIcon);
            View vConnector = statusView.findViewById(R.id.vConnector);
            TextView tvTitle = statusView.findViewById(R.id.tvStatusTitle);
            TextView tvDate = statusView.findViewById(R.id.tvStatusDate);
            TextView tvDesc = statusView.findViewById(R.id.tvStatusDescription);

            ivIcon.setImageResource(status.getIconRes());
            tvTitle.setText(status.getTitle() != null ? status.getTitle() : "");
            tvDate.setText(status.getDate() != null ? status.getDate() : "");
            tvDesc.setText(status.getDescription() != null ? status.getDescription() : "");

            if (currentStatusIndex < trackingStatuses.size() - 1) {
                vConnector.setVisibility(View.VISIBLE);
            }

            if (status.isCurrent()) {
                ivIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                tvTitle.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            } else {
                ivIcon.setColorFilter(ContextCompat.getColor(this, R.color.secondary_text), PorterDuff.Mode.SRC_IN);
            }

            timeline.addView(statusView);

            if (status.getTitle() != null && status.getTitle().equals("Delivered")) {
                TextView tvOrderStatus = findViewById(R.id.tvOrderStatus);
                tvOrderStatus.setText("Delivered");
                tvOrderStatus.setTextColor(ContextCompat.getColor(this, R.color.green));
                trackingHandler.removeCallbacks(trackingRunnable);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating tracking status", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (trackingHandler != null && trackingRunnable != null) {
            trackingHandler.removeCallbacks(trackingRunnable);
        }
    }
}