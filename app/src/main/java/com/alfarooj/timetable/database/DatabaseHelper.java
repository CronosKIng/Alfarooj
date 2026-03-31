package com.alfarooj.timetable.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.models.AttendanceLog;
import com.alfarooj.timetable.utils.TimeHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "alfarooj.db";
    private static final int DATABASE_VERSION = 7;

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

        // Insert SUPER ADMIN
        String INSERT_SUPER_ADMIN = "INSERT INTO users (full_name, username, password, role, department) VALUES " +
            "('AL FAROOJ AL SHAMI', 'ALFAROOJ', '097321494', 'super_admin', 'admin')";
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
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", new String[]{username, password});
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isValid;
    }

    public User getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(cursor.getInt(0), cursor.getString(1), cursor.getString(2), 
                cursor.getString(3), cursor.getString(4), cursor.getString(5), 
                cursor.getInt(6), cursor.getString(7));
        }
        cursor.close();
        db.close();
        return user;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(cursor.getInt(0), cursor.getString(1), cursor.getString(2), 
                cursor.getString(3), cursor.getString(4), cursor.getString(5), 
                cursor.getInt(6), cursor.getString(7));
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
            users.add(new User(cursor.getInt(0), cursor.getString(1), cursor.getString(2), 
                cursor.getString(3), cursor.getString(4), cursor.getString(5), 
                cursor.getInt(6), cursor.getString(7)));
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

    public boolean insertAttendanceLog(int userId, String username, String fullName, String department, 
                                       String eventType, String eventName, String location, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("username", username);
        values.put("full_name", fullName);
        values.put("department", department);
        values.put("event_type", eventType);
        values.put("event_name", eventName);
        values.put("location", location);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        // Use UAE time
        values.put("timestamp", TimeHelper.getCurrentDateTime());
        long result = db.insert("attendance_logs", null, values);
        db.close();
        return result != -1;
    }

    public ArrayList<AttendanceLog> getAllAttendanceLogs() {
        ArrayList<AttendanceLog> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM attendance_logs ORDER BY id DESC LIMIT 200", null);
        while (cursor.moveToNext()) {
            logs.add(new AttendanceLog(
                cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7),
                cursor.getDouble(8), cursor.getDouble(9), cursor.getString(10)
            ));
        }
        cursor.close();
        db.close();
        return logs;
    }

    public ArrayList<AttendanceLog> getAttendanceLogsByDepartment(String department) {
        ArrayList<AttendanceLog> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM attendance_logs WHERE department = ? ORDER BY id DESC LIMIT 200", 
            new String[]{department});
        while (cursor.moveToNext()) {
            logs.add(new AttendanceLog(
                cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7),
                cursor.getDouble(8), cursor.getDouble(9), cursor.getString(10)
            ));
        }
        cursor.close();
        db.close();
        return logs;
    }

    public ArrayList<AttendanceLog> getTodayAttendanceLogs() {
        ArrayList<AttendanceLog> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String todayDate = TimeHelper.getCurrentDate();
        Cursor cursor = db.rawQuery("SELECT * FROM attendance_logs WHERE date(timestamp) = ? ORDER BY id DESC", 
            new String[]{todayDate});
        while (cursor.moveToNext()) {
            logs.add(new AttendanceLog(
                cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7),
                cursor.getDouble(8), cursor.getDouble(9), cursor.getString(10)
            ));
        }
        cursor.close();
        db.close();
        return logs;
    }
}
