package com.joseph.e_electronicshop.ui.Adapters;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;
import java.util.Map;

public class Product {
    // Firestore document ID (excluded from Firestore fields)
    @Exclude
    private String id;

    // Product fields (must match Firestore field names)
    private String productName;
    private String priceKsh;
    private String priceUsd;
    private String discount;
    private String discountType;
    private String description;
    private String category;
    private String imageBase64;
    private long timestamp;


    // Required empty constructor for Firestore
    public Product() {}

    // Constructor with parameters (optional)
    public Product(String productName, String priceKsh, String priceUsd, String discount,
                   String discountType, String description, String category, String imageBase64) {
        this.productName = productName;
        this.priceKsh = priceKsh;
        this.priceUsd = priceUsd;
        this.discount = discount;
        this.discountType = discountType;
        this.description = description;
        this.category = category;
        this.imageBase64 = imageBase64;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters



    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("productName")
    public String getProductName() {
        return productName;
    }

    @PropertyName("productName")
    public void setProductName(String productName) {
        this.productName = productName;
    }

    @PropertyName("priceKsh")
    public String getPriceKsh() {
        return priceKsh;
    }

    @PropertyName("priceKsh")
    public void setPriceKsh(String priceKsh) {
        this.priceKsh = priceKsh;
    }

    @PropertyName("priceUsd")
    public String getPriceUsd() {
        return priceUsd;
    }

    @PropertyName("priceUsd")
    public void setPriceUsd(String priceUsd) {
        this.priceUsd = priceUsd;
    }

    @PropertyName("discount")
    public String getDiscount() {
        return discount;
    }

    @PropertyName("discount")
    public void setDiscount(String discount) {
        this.discount = discount;
    }

    @PropertyName("discountType")
    public String getDiscountType() {
        return discountType;
    }

    @PropertyName("discountType")
    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("category")
    public String getCategory() {
        return category;
    }

    @PropertyName("category")
    public void setCategory(String category) {
        this.category = category;
    }

    @PropertyName("imageBase64")
    public String getImageBase64() {
        return imageBase64;
    }

    @PropertyName("imageBase64")
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }




    // Helper method to convert to Map for Firestore
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("productName", productName);
        map.put("priceKsh", priceKsh);
        map.put("priceUsd", priceUsd);
        map.put("discount", discount);
        map.put("discountType", discountType);
        map.put("description", description);
        map.put("category", category);
        map.put("imageBase64", imageBase64);
        map.put("timestamp", timestamp);
        //cart
        map.put("inCart", inCart);
        map.put("cartTimestamp", cartTimestamp);

        return map;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }



    // Add these fields to your Product class
    @PropertyName("inCart")
    private boolean inCart = false;

    @PropertyName("cartTimestamp")
    private long cartTimestamp = 0L;

    // Add getters and setters
    @PropertyName("inCart")
    public boolean isInCart() {
        return inCart;
    }

    @PropertyName("inCart")
    public void setInCart(boolean inCart) {
        this.inCart = inCart;
    }

    @PropertyName("cartTimestamp")
    public long getCartTimestamp() {
        return cartTimestamp;
    }

    @PropertyName("cartTimestamp")
    public void setCartTimestamp(long cartTimestamp) {
        this.cartTimestamp = cartTimestamp;
    }



}