package com.alfarooj.timetable.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeHelper {
    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Africa/Dar_es_Salaam"));
        return sdf.format(new Date());
    }
    
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Africa/Dar_es_Salaam"));
        return sdf.format(new Date());
    }
    
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Africa/Dar_es_Salaam"));
        return sdf.format(new Date());
    }
    
    public static String formatTimestamp(String timestamp) {
        return timestamp;
    }
}
