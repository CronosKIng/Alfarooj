package com.alfarooj.timetable.models;

public class AttendanceLog {
    private int id;
    private int user_id;
    private String username;
    private String full_name;
    private String department;
    private String event_type;
    private String event_name;
    private String location;
    private double latitude;
    private double longitude;
    private String timestamp;
    
    // Getters
    public int getId() { return id; }
    public int getUserId() { return user_id; }
    public String getUsername() { return username; }
    public String getFullName() { return full_name; }
    public String getDepartment() { return department; }
    public String getEventType() { return event_type; }
    public String getEventName() { return event_name; }
    public String getLocation() { return location; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getTimestamp() { return timestamp; }
}
