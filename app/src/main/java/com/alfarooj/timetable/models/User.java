package com.alfarooj.timetable.models;

import com.google.gson.annotations.SerializedName;

public class User {
    private int id;
    private String full_name;
    private String username;
    private String role;
    private String department;
    
    public User() {}
    
    public int getId() { return id; }
    public String getFullName() { return full_name; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
}
