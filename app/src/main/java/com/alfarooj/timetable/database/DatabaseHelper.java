package com.alfarooj.timetable.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.models.AttendanceLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "alfarooj.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TIME_ZONE = "+03:00";
    
    private static final String SUPER_ADMIN_USERNAME = "AL FAROOJ AL SHAMI MUWAILEH";
    private static final String SUPER_ADMIN_PASSWORD = "097321494";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "full_name TEXT NOT NULL," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "department TEXT," +
                "created_by INTEGER," +
                "created_at TEXT DEFAULT (datetime('now', 'localtime')))";
        db.execSQL(CREATE_USERS_TABLE);
        
        String CREATE_ATTENDANCE_TABLE = "CREATE TABLE attendance_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "username TEXT," +
                "full_name TEXT," +
                "department TEXT," +
                "event_type TEXT," +
                "event_name TEXT," +
                "location TEXT," +
                "latitude REAL," +
                "longitude REAL," +
                "timestamp TEXT DEFAULT (datetime('now', 'localtime')))";
        db.execSQL(CREATE_ATTENDANCE_TABLE);
        
        String INSERT_SUPER_ADMIN = "INSERT INTO users (full_name, username, password, role) VALUES ('" +
                SUPER_ADMIN_USERNAME + "', '" + SUPER_ADMIN_USERNAME + "', '" + 
                SUPER_ADMIN_PASSWORD + "', 'super_admin')";
        db.execSQL(INSERT_SUPER_ADMIN);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS attendance_logs");
        onCreate(db);
    }
    
    public boolean login(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isValid;
    }
    
    public String getUserRole(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT role FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        String role = "user";
        if (cursor.moveToFirst()) {
            role = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return role;
    }
    
    public User getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getInt(6),
                cursor.getString(7)
            );
        }
        cursor.close();
        db.close();
        return user;
    }
    
    public ArrayList<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE role != 'super_admin' ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            users.add(new User(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getInt(6),
                cursor.getString(7)
            ));
        }
        cursor.close();
        db.close();
        return users;
    }
    
    public ArrayList<User> getUsersByDepartment(String department) {
        ArrayList<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE department = ? AND role = 'user'", new String[]{department});
        while (cursor.moveToNext()) {
            users.add(new User(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getInt(6),
                cursor.getString(7)
            ));
        }
        cursor.close();
        db.close();
        return users;
    }
    
    public boolean createUser(String fullName, String username, String password, String role, String department, int createdBy) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("username", username);
        values.put("password", password);
        values.put("role", role);
        values.put("department", department);
        values.put("created_by", createdBy);
        long result = db.insert("users", null, values);
        db.close();
        return result != -1;
    }
    
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("users", "id = ?", new String[]{String.valueOf(userId)});
        db.close();
        return result > 0;
    }
    
    public boolean insertAttendanceLog(int userId, String username, String fullName, String department, String eventType, String eventName, String location, double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("username", username);
        values.put("full_name", fullName);
        values.put("department", department);
        values.put("event_type", eventType);
        values.put("event_name", eventName);
        values.put("location", location);
        values.put("latitude", lat);
        values.put("longitude", lng);
        long result = db.insert("attendance_logs", null, values);
        db.close();
        return result != -1;
    }
    
    public ArrayList<AttendanceLog> getAllAttendanceLogs() {
        ArrayList<AttendanceLog> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM attendance_logs ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            logs.add(new AttendanceLog(
                cursor.getInt(0),
                cursor.getInt(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getDouble(8),
                cursor.getDouble(9),
                cursor.getString(10)
            ));
        }
        cursor.close();
        db.close();
        return logs;
    }
    
    public ArrayList<AttendanceLog> getTodayAttendanceLogs() {
        ArrayList<AttendanceLog> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor cursor = db.rawQuery("SELECT * FROM attendance_logs WHERE date(timestamp) = date('now', 'localtime') ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            logs.add(new AttendanceLog(
                cursor.getInt(0),
                cursor.getInt(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getDouble(8),
                cursor.getDouble(9),
                cursor.getString(10)
            ));
        }
        cursor.close();
        db.close();
        return logs;
    }
    
    public ArrayList<AttendanceLog> getUserAttendanceLogs(int userId) {
        ArrayList<AttendanceLog> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM attendance_logs WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)});
        while (cursor.moveToNext()) {
            logs.add(new AttendanceLog(
                cursor.getInt(0),
                cursor.getInt(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getDouble(8),
                cursor.getDouble(9),
                cursor.getString(10)
            ));
        }
        cursor.close();
        db.close();
        return logs;
    }
}
