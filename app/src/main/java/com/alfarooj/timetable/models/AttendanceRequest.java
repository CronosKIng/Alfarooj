package com.alfarooj.timetable.models;

import com.google.gson.annotations.SerializedName;

public class AttendanceRequest {
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("full_name")
    private String fullName;
    
    @SerializedName("department")
    private String department;
    
    @SerializedName("event_type")
    private String eventType;
    
    @SerializedName("event_name")
    private String eventName;
    
    @SerializedName("latitude")
    private double latitude;
    
    @SerializedName("longitude")
    private double longitude;
    
    @SerializedName("location")
    private String location;
    
    @SerializedName("comment")
    private String comment;
    
    @SerializedName("break_type")
    private String breakType;
    
    @SerializedName("order_type")
    private String orderType;

    public AttendanceRequest(int userId, String username, String fullName, String department,
                             String eventType, String eventName, double latitude, double longitude, String location) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.department = department;
        this.eventType = eventType;
        this.eventName = eventName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.comment = "";
        this.breakType = "";
        this.orderType = "";
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public void setBreakType(String breakType) {
        this.breakType = breakType;
    }
    
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
    
    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getDepartment() { return department; }
    public String getEventType() { return eventType; }
    public String getEventName() { return eventName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getLocation() { return location; }
    public String getComment() { return comment; }
    public String getBreakType() { return breakType; }
    public String getOrderType() { return orderType; }
}
