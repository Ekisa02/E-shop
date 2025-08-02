package com.joseph.e_electronicshop.ui.Adapters;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Order implements Serializable {
    @Exclude
    private String id;

    @PropertyName("orderDate")
    private Date orderDate;

    @PropertyName("status")
    private String status;

    @PropertyName("items")
    private List<Product> items;

    @PropertyName("subtotal")
    private double subtotal;

    @PropertyName("shipping")
    private double shipping;

    @PropertyName("tax")
    private double tax;

    @PropertyName("total")
    private double total;

    @PropertyName("shippingAddress")
    private String shippingAddress;

    @PropertyName("contactNumber")
    private String contactNumber;

    @PropertyName("paymentMethod")
    private String paymentMethod;

    @PropertyName("trackingNumber")
    private String trackingNumber;

    @PropertyName("userId")
    private String userId; // Link to user who placed the order

    // NEW METHODS ADD HERE
    @Exclude
    public String getOrderId() {
        return this.id;
    }

    @Exclude
    public void setOrderId(String orderId) {
        this.id = orderId;
    }


    @PropertyName("timestamp")
    private long timestamp;

    public Order() {
        this.id = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.orderDate = new Date();
        this.status = "Placed";
        this.items = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }

    // Firestore compatible getters and setters
    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("orderDate")
    public Date getOrderDate() {
        return orderDate;
    }

    @PropertyName("orderDate")
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @PropertyName("items")
    public List<Product> getItems() {
        return items;
    }

    @PropertyName("items")
    public void setItems(List<Product> items) {
        this.items = items;
        calculateTotals();
    }

    public void addItem(Product product) {
        this.items.add(product);
        calculateTotals();
    }

    @PropertyName("subtotal")
    public double getSubtotal() {
        return subtotal;
    }

    @PropertyName("subtotal")
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    @PropertyName("shipping")
    public double getShipping() {
        return shipping;
    }

    @PropertyName("shipping")
    public void setShipping(double shipping) {
        this.shipping = shipping;
        calculateTotals();
    }

    @PropertyName("tax")
    public double getTax() {
        return tax;
    }

    @PropertyName("tax")
    public void setTax(double tax) {
        this.tax = tax;
        calculateTotals();
    }

    @PropertyName("total")
    public double getTotal() {
        return total;
    }

    @PropertyName("total")
    public void setTotal(double total) {
        this.total = total;
    }

    @PropertyName("shippingAddress")
    public String getShippingAddress() {
        return shippingAddress;
    }

    @PropertyName("shippingAddress")
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    @PropertyName("contactNumber")
    public String getContactNumber() {
        return contactNumber;
    }

    @PropertyName("contactNumber")
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    @PropertyName("paymentMethod")
    public String getPaymentMethod() {
        return paymentMethod;
    }

    @PropertyName("paymentMethod")
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @PropertyName("trackingNumber")
    public String getTrackingNumber() {
        return trackingNumber;
    }

    @PropertyName("trackingNumber")
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    private void calculateTotals() {
        subtotal = 0;
        for (Product item : items) {
            // Using price in KSH (convert String to double)
            subtotal += Double.parseDouble(item.getPriceKsh()) * (item.isInCart() ? 1 : 0);
        }

        // Default shipping calculation
        if (shipping == 0) {
            shipping = subtotal > 5000 ? 0 : 200; // Free shipping over 5000 KSH
        }

        // Default tax calculation (16% VAT in Kenya)
        if (tax == 0) {
            tax = subtotal * 0.16;
        }

        total = subtotal + shipping + tax;
    }

    @Exclude
    public String getFormattedOrderDate() {
        return android.text.format.DateFormat.format("MMM dd, yyyy hh:mm a", orderDate).toString();
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("orderDate", orderDate);
        map.put("status", status);
        map.put("items", items);
        map.put("subtotal", subtotal);
        map.put("shipping", shipping);
        map.put("tax", tax);
        map.put("total", total);
        map.put("shippingAddress", shippingAddress);
        map.put("contactNumber", contactNumber);
        map.put("paymentMethod", paymentMethod);
        map.put("trackingNumber", trackingNumber);
        map.put("userId", userId);
        map.put("timestamp", timestamp);
        return map;
    }
}