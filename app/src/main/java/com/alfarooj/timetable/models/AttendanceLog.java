package com.alfarooj.timetable.models;

public class AttendanceLog {
    private int id;
    private int userId;
    private String username;
    private String fullName;
    private String department;
    private String eventType;
    private String eventName;
    private String location;
    private double latitude;
    private double longitude;
    private String timestamp;
    
    public AttendanceLog(int id, int userId, String username, String fullName, String department, String eventType, String eventName, String location, double latitude, double longitude, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.department = department;
        this.eventType = eventType;
        this.eventName = eventName;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
    
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getDepartment() { return department; }
    public String getEventType() { return eventType; }
    public String getEventName() { return eventName; }
    public String getLocation() { return location; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getTimestamp() { return timestamp; }
}
