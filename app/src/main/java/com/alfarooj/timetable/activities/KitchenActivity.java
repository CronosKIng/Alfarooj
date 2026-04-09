package com.alfarooj.timetable.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.AttendanceRequest;
import com.alfarooj.timetable.models.AttendanceResponse;
import com.alfarooj.timetable.models.LocationResponse;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KitchenActivity extends BaseActivity {
    private Button btnSignIn, btnSignOut, btnViewHistory, btnLogout;
    private TextView tvWelcome, tvStatus;
    private SessionManager session;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen);

        session = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatus = findViewById(R.id.tvStatus);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnLogout = findViewById(R.id.btnLogout);

        // Get department from session (what user was assigned)
        String department = session.getDepartment();
        tvWelcome.setText("User: " + session.getFullName() + " (" + getDepartmentDisplay(department) + ")");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }

        btnSignIn.setOnClickListener(v -> checkLocationAndProceed("sign_in", "Sign In"));
        btnSignOut.setOnClickListener(v -> checkLocationAndProceed("sign_out", "Sign Out"));
        btnViewHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> logout());
    }
    
    private String getDepartmentDisplay(String dept) {
        if (dept == null) return "";
        switch(dept) {
            case "kitchen": return "Kitchen";
            case "waiter": return "Waiter";
            case "delivery": return "Delivery";
            case "manager": return "Manager";
            default: return dept;
        }
    }
    
    private void checkLocationAndProceed(String eventType, String eventName) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required!", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        
        tvStatus.setText("Checking location...");
        
        LocationRequest locationRequest = new LocationRequest.Builder(10000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build();
            
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    fusedLocationClient.removeLocationUpdates(this);
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    validateLocationWithApi(eventType, eventName);
                } else {
                    tvStatus.setText("Cannot get location. Try again.");
                    Toast.makeText(KitchenActivity.this, "Cannot get location", Toast.LENGTH_SHORT).show();
                }
            }
        };
        
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    
    private void validateLocationWithApi(String eventType, String eventName) {
        com.alfarooj.timetable.models.LocationRequest locationRequest = 
            new com.alfarooj.timetable.models.LocationRequest(currentLatitude, currentLongitude);
            
        ApiClient.getApiService().validateLocation(locationRequest)
            .enqueue(new Callback<LocationResponse>() {
                @Override
                public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isWithinLocation()) {
                        recordAttendance(eventType, eventName);
                    } else {
                        tvStatus.setText("You are NOT at the work location!");
                        Toast.makeText(KitchenActivity.this, "You are NOT at the work location!", Toast.LENGTH_LONG).show();
                    }
                }
                
                @Override
                public void onFailure(Call<LocationResponse> call, Throwable t) {
                    tvStatus.setText("Location validation failed");
                    Toast.makeText(KitchenActivity.this, "Location validation failed", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void recordAttendance(String eventType, String eventName) {
        // Use department from session (the user's assigned department)
        String department = session.getDepartment();
        String location = "Lat: " + currentLatitude + ", Lon: " + currentLongitude;
        
        AttendanceRequest request = new AttendanceRequest(
            session.getUserId(), session.getUsername(), session.getFullName(),
            department, eventType, eventName, currentLatitude, currentLongitude, location
        );
        
        ApiClient.getApiService().recordAttendance(request)
            .enqueue(new Callback<AttendanceResponse>() {
                @Override
                public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        tvStatus.setText(eventName + " recorded!");
                        Toast.makeText(KitchenActivity.this, eventName + " Success!", Toast.LENGTH_SHORT).show();
                    } else {
                        tvStatus.setText("Failed to record");
                        Toast.makeText(KitchenActivity.this, "Failed to record!", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                    tvStatus.setText("Network error");
                    Toast.makeText(KitchenActivity.this, "Network error!", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission required!", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void logout() {
        session.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
