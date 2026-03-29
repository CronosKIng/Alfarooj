package com.alfarooj.timetable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.LocationHelper;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TimeHelper;
import com.alfarooj.timetable.R;

public class UserActivity extends BaseActivity {
    private Button btnSignIn, btnBreakStart, btnBreakEnd, btnDeliveryGo, btnDeliveryReturn, btnSignOut, btnViewHistory, btnLogout;
    private TextView tvWelcome, tvStatus;
    private DatabaseHelper db;
    private SessionManager session;
    private LocationHelper locationHelper;
    private User currentUser;
    private String department;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        locationHelper = new LocationHelper(this);
        currentUser = db.getUser(session.getUsername());
        department = session.getDepartment();
        
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatus = findViewById(R.id.tvStatus);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnBreakStart = findViewById(R.id.btnBreakStart);
        btnBreakEnd = findViewById(R.id.btnBreakEnd);
        btnDeliveryGo = findViewById(R.id.btnDeliveryGo);
        btnDeliveryReturn = findViewById(R.id.btnDeliveryReturn);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnLogout = findViewById(R.id.btnLogout);
        
        tvWelcome.setText(getString(R.string.welcome) + " " + session.getFullName() + " - " + getDepartmentDisplay());
        
        // Set button texts
        btnSignIn.setText(getString(R.string.sign_in));
        btnBreakStart.setText(getString(R.string.break_start));
        btnBreakEnd.setText(getString(R.string.break_end));
        btnDeliveryGo.setText(getString(R.string.delivery_go));
        btnDeliveryReturn.setText(getString(R.string.delivery_return));
        btnSignOut.setText(getString(R.string.sign_out));
        btnViewHistory.setText(getString(R.string.history));
        btnLogout.setText(getString(R.string.logout));
        
        setupButtonsByDepartment();
        
        btnSignIn.setOnClickListener(v -> logEvent("sign_in", getString(R.string.sign_in)));
        btnBreakStart.setOnClickListener(v -> logEvent("break_start", getString(R.string.break_start)));
        btnBreakEnd.setOnClickListener(v -> logEvent("break_end", getString(R.string.break_end)));
        btnDeliveryGo.setOnClickListener(v -> logEvent("delivery_go", getString(R.string.delivery_go)));
        btnDeliveryReturn.setOnClickListener(v -> logEvent("delivery_return", getString(R.string.delivery_return)));
        btnSignOut.setOnClickListener(v -> logEvent("sign_out", getString(R.string.sign_out)));
        btnViewHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> {
            session.logout();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
    
    private String getDepartmentDisplay() {
        switch(department) {
            case "kitchen": return getString(R.string.kitchen);
            case "waiters": return getString(R.string.waiters);
            case "delivery": return getString(R.string.delivery);
            case "managers": return getString(R.string.managers);
            default: return department;
        }
    }
    
    private void setupButtonsByDepartment() {
        if (department.equals("delivery")) {
            btnDeliveryGo.setVisibility(View.VISIBLE);
            btnDeliveryReturn.setVisibility(View.VISIBLE);
        } else {
            btnDeliveryGo.setVisibility(View.GONE);
            btnDeliveryReturn.setVisibility(View.GONE);
        }
        
        if (department.equals("managers")) {
            btnBreakStart.setVisibility(View.GONE);
            btnBreakEnd.setVisibility(View.GONE);
        }
    }
    
    private void logEvent(String eventType, String eventName) {
        locationHelper.checkLocationAndProceed(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationSuccess(double latitude, double longitude, String address) {
                boolean saved = db.insertAttendanceLog(
                    session.getUserId(), session.getUsername(), session.getFullName(),
                    department, eventType, eventName, address, latitude, longitude
                );
                
                if (saved) {
                    tvStatus.setText(eventName + " " + getString(R.string.success) + " " + TimeHelper.getCurrentTime());
                    Toast.makeText(UserActivity.this, eventName + " " + getString(R.string.success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onLocationFailed(String error) {
                tvStatus.setText(getString(R.string.location_error));
                Toast.makeText(UserActivity.this, getString(R.string.location_error), Toast.LENGTH_LONG).show();
            }
        }, () -> {});
    }
}
