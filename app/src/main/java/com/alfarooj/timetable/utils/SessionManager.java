package com.alfarooj.timetable.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "AlfaroojSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_DEPARTMENT = "department";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    public void createLoginSession(int userId, String username, String fullName, String role, String department) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_DEPARTMENT, department);
        editor.commit();
    }
    
    public int getUserId() { return pref.getInt(KEY_USER_ID, -1); }
    public String getUsername() { return pref.getString(KEY_USERNAME, null); }
    public String getFullName() { return pref.getString(KEY_FULL_NAME, null); }
    public String getRole() { return pref.getString(KEY_ROLE, null); }
    public String getDepartment() { return pref.getString(KEY_DEPARTMENT, null); }
    public boolean isLoggedIn() { return pref.getBoolean(KEY_IS_LOGGED_IN, false); }
    
    public void logout() {
        editor.clear();
        editor.commit();
    }
}
