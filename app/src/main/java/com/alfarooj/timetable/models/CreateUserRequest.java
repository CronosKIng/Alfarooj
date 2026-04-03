package com.alfarooj.timetable.models;

public class CreateUserRequest {
    private String full_name;
    private String username;
    private String password;
    private String role;
    private String department;
    private int created_by;
    
    public CreateUserRequest(String full_name, String username, String password, String role, String department, int created_by) {
        this.full_name = full_name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = department;
        this.created_by = created_by;
    }
}
