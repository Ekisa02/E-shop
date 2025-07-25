package com.joseph.e_electronicshop.ui.home;

import java.util.Date;

import java.util.Date;

public class Product {
    private String id;
    private String name;
    private String category;
    private double priceKsh;
    private double priceUsd;
    private double discountAmount;
    private String discountType;
    private String description;
    private String imageUrl;
    private Date createdAt;

    public Product() {
        // Required empty constructor for Firestore
    }

    public Product(String name, String category, double priceKsh, double priceUsd,
                   double discountAmount, String discountType, String description, String imageUrl) {
        this.name = name;
        this.category = category;
        this.priceKsh = priceKsh;
        this.priceUsd = priceUsd;
        this.discountAmount = discountAmount;
        this.discountType = discountType;
        this.description = description;
        this.imageUrl = imageUrl;
        this.createdAt = new Date();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPriceKsh() { return priceKsh; }
    public void setPriceKsh(double priceKsh) { this.priceKsh = priceKsh; }
    public double getPriceUsd() { return priceUsd; }
    public void setPriceUsd(double priceUsd) { this.priceUsd = priceUsd; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}