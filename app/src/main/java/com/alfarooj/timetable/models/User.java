package com.alfarooj.timetable.models;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String full_name;
    private String username;
    private String password;
    private String role;
    private String department;
    private int created_by;
    private String created_at;
    
    public User() {}
    
    public User(int id, String full_name, String username, String password, String role, String department, int created_by, String created_at) {
        this.id = id;
        this.full_name = full_name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = department;
        this.created_by = created_by;
        this.created_at = created_at;
    }
    
    public int getId() { return id; }
    public String getFullName() { return full_name; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
    public int getCreatedBy() { return created_by; }
    public String getCreatedAt() { return created_at; }
    
    public String getDepartmentDisplay() {
        if (department == null) return "-";
        switch(department) {
            case "kitchen": return "Kitchen";
            case "waiter": return "Waiter";
            case "delivery": return "Delivery";
            case "manager": return "Manager";
            default: return department;
        }
    }
}
