package com.alfarooj.timetable.models;

import java.util.List;

public class UsersResponse {
    private boolean success;
    private List<User> users;
    
    public boolean isSuccess() { return success; }
    public List<User> getUsers() { return users; }
}
