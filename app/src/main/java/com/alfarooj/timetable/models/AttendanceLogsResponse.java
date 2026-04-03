package com.alfarooj.timetable.models;

import java.util.List;

public class AttendanceLogsResponse {
    private boolean success;
    private List<AttendanceLog> logs;
    
    public boolean isSuccess() { return success; }
    public List<AttendanceLog> getLogs() { return logs; }
}
