package com.alfarooj.timetable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.utils.LocationHelper;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TimeHelper;
import com.alfarooj.timetable.R;

public class UserActivity extends BaseActivity {
    private Button btnSignIn, btnSignOut, btnViewHistory, btnLogout;
    private TextView tvWelcome, tvStatus;
    private DatabaseHelper db;
    private SessionManager session;
    private LocationHelper locationHelper;
    private String department;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        
        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        locationHelper = new LocationHelper(this);
        department = session.getDepartment();
        
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatus = findViewById(R.id.tvStatus);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnLogout = findViewById(R.id.btnLogout);
        
        tvWelcome.setText("Welcome " + session.getFullName());
        
        btnSignIn.setOnClickListener(v -> logEvent("sign_in", "Sign In"));
        btnSignOut.setOnClickListener(v -> logEvent("sign_out", "Sign Out"));
        btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
        btnLogout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(UserActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void logEvent(String eventType, String eventName) {
        locationHelper.checkLocationAndProceed(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationSuccess(double latitude, double longitude, String address) {
                boolean saved = db.insertAttendanceLog(session.getUserId(), session.getUsername(), session.getFullName(), department, eventType, eventName, address, latitude, longitude);
                if (saved) {
                    tvStatus.setText(eventName + " recorded at " + TimeHelper.getCurrentTime());
                    Toast.makeText(UserActivity.this, eventName + " Success!", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onLocationFailed(String error) {
                tvStatus.setText(error);
                Toast.makeText(UserActivity.this, error, Toast.LENGTH_LONG).show();
            }
        }, () -> {});
    }
}
