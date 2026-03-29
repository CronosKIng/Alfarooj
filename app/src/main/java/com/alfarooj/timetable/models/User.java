package com.alfarooj.timetable.models;

public class User {
    private int id;
    private String fullName;
    private String username;
    private String password;
    private String role;
    private String department;
    private int createdBy;
    private String createdAt;
    
    public User(int id, String fullName, String username, String password, String role, String department, int createdBy, String createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = department;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
    
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
    public int getCreatedBy() { return createdBy; }
    public String getCreatedAt() { return createdAt; }
    
    public String getDepartmentDisplay() {
        if (department == null) return "-";
        switch(department) {
            case "kitchen": return "Jikoni";
            case "waiters": return "Wahudumu";
            case "delivery": return "Delivery";
            case "managers": return "Viongozi";
            default: return department;
        }
    }
}
