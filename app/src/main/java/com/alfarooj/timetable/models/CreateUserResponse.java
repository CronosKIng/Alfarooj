package com.alfarooj.timetable.models;

public class CreateUserResponse {
    private boolean success;
    private String message;
    private int user_id;
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getUserId() { return user_id; }
}
