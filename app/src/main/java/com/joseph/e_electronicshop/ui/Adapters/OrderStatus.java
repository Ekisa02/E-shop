package com.joseph.e_electronicshop.ui.Adapters;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OrderStatus implements Serializable {
    @PropertyName("title")
    private String title;

    @PropertyName("date")
    private String date;

    @PropertyName("description")
    private String description;

    @Exclude
    private int iconRes;

    @PropertyName("isCurrent")
    private boolean isCurrent;

    @PropertyName("timestamp")
    private long timestamp;

    public OrderStatus() {
        this.timestamp = System.currentTimeMillis();
    }

    public OrderStatus(String title, String date, String description, int iconRes, boolean isCurrent) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.iconRes = iconRes;
        this.isCurrent = isCurrent;
        this.timestamp = System.currentTimeMillis();
    }

    // Firestore compatible getters and setters
    @PropertyName("title")
    public String getTitle() {
        return title;
    }

    @PropertyName("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("date")
    public String getDate() {
        return date;
    }

    @PropertyName("date")
    public void setDate(String date) {
        this.date = date;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @Exclude
    public int getIconRes() {
        return iconRes;
    }

    @Exclude
    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    @PropertyName("isCurrent")
    public boolean isCurrent() {
        return isCurrent;
    }

    @PropertyName("isCurrent")
    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("date", date);
        map.put("description", description);
        map.put("isCurrent", isCurrent);
        map.put("timestamp", timestamp);
        return map;
    }
}