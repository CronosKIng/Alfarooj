package com.alfarooj.timetable.models;

public class LocationResponse {
    private boolean success;
    private String message;
    private boolean within_location;
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public boolean isWithinLocation() { return within_location; }
}
