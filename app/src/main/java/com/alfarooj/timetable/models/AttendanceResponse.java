package com.alfarooj.timetable.models;

public class AttendanceResponse {
    private boolean success;
    private String message;
    private String timestamp;
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}
