package com.alfarooj.timetable.models;

import java.io.Serializable;

public class UpdateDepartmentRequest implements Serializable {
    private int user_id;
    private String department;
    
    public UpdateDepartmentRequest(int user_id, String department) {
        this.user_id = user_id;
        this.department = department;
    }
    
    public int getUser_id() { return user_id; }
    public String getDepartment() { return department; }
}
