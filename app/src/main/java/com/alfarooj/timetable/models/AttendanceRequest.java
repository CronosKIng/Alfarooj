package com.alfarooj.timetable.models;

public class AttendanceRequest {
    private int user_id;
    private String username;
    private String full_name;
    private String department;
    private String event_type;
    private String event_name;
    private double latitude;
    private double longitude;
    private String location;
    
    public AttendanceRequest(int user_id, String username, String full_name, String department, 
                             String event_type, String event_name, double latitude, double longitude, String location) {
        this.user_id = user_id;
        this.username = username;
        this.full_name = full_name;
        this.department = department;
        this.event_type = event_type;
        this.event_name = event_name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
    }
}
