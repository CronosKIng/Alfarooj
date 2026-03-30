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

public class WaiterActivity extends BaseActivity {
    private Button btnSignIn, btnBreakStart, btnBreakEnd, btnSignOut, btnViewHistory, btnLogout;
    private TextView tvWelcome, tvStatus;
    private DatabaseHelper db;
    private SessionManager session;
    private LocationHelper locationHelper;
    private String department = "waiter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiter);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        locationHelper = new LocationHelper(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatus = findViewById(R.id.tvStatus);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnBreakStart = findViewById(R.id.btnBreakStart);
        btnBreakEnd = findViewById(R.id.btnBreakEnd);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnLogout = findViewById(R.id.btnLogout);

        tvWelcome.setText("WELCOME WAITER - " + session.getFullName());

        btnSignIn.setOnClickListener(v -> logEvent("sign_in", "Sign In"));
        btnBreakStart.setOnClickListener(v -> logEvent("break_start", "Break Start"));
        btnBreakEnd.setOnClickListener(v -> logEvent("break_end", "Break End"));
        btnSignOut.setOnClickListener(v -> logEvent("sign_out", "Sign Out"));
        btnViewHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logEvent(String eventType, String eventName) {
        locationHelper.checkLocationAndProceed(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationSuccess(double latitude, double longitude, String address) {
                boolean saved = db.insertAttendanceLog(session.getUserId(), session.getUsername(), 
                    session.getFullName(), department, eventType, eventName, address, latitude, longitude);
                if (saved) {
                    tvStatus.setText(eventName + " - " + TimeHelper.getCurrentTime());
                    Toast.makeText(WaiterActivity.this, eventName + " Success!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onLocationFailed(String error) {
                tvStatus.setText(error);
                Toast.makeText(WaiterActivity.this, error, Toast.LENGTH_LONG).show();
            }
        }, () -> {});
    }

    private void logout() {
        session.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
