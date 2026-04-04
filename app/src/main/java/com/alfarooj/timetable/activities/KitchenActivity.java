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
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KitchenActivity extends BaseActivity {
    private Button btnSignIn, btnBreakStart, btnBreakEnd, btnSignOut, btnViewHistory, btnLogout;
    private TextView tvWelcome, tvStatus;
    private SessionManager session;
    private FusedLocationProviderClient fusedLocationClient;
    private String department = "kitchen";
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private String currentAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen);

        try {
            session = new SessionManager(this);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            tvWelcome = findViewById(R.id.tvWelcome);
            tvStatus = findViewById(R.id.tvStatus);
            btnSignIn = findViewById(R.id.btnSignIn);
            btnBreakStart = findViewById(R.id.btnBreakStart);
            btnBreakEnd = findViewById(R.id.btnBreakEnd);
            btnSignOut = findViewById(R.id.btnSignOut);
            btnViewHistory = findViewById(R.id.btnViewHistory);
            btnLogout = findViewById(R.id.btnLogout);

            if (tvWelcome != null) {
                tvWelcome.setText("WELCOME KITCHEN - " + session.getFullName());
            }
            if (tvStatus != null) {
                tvStatus.setText("Ready to sign in");
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            }

            if (btnSignIn != null) btnSignIn.setOnClickListener(v -> checkLocationAndProceed("sign_in", "Sign In"));
            if (btnBreakStart != null) btnBreakStart.setOnClickListener(v -> checkLocationAndProceed("break_start", "Break Start"));
            if (btnBreakEnd != null) btnBreakEnd.setOnClickListener(v -> checkLocationAndProceed("break_end", "Break End"));
            if (btnSignOut != null) btnSignOut.setOnClickListener(v -> checkLocationAndProceed("sign_out", "Sign Out"));
            if (btnViewHistory != null) btnViewHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
            if (btnLogout != null) btnLogout.setOnClickListener(v -> logout());
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh UI if needed
    }
    
    private void checkLocationAndProceed(String eventType, String eventName) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required!", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        
        if (tvStatus != null) tvStatus.setText("Checking location...");
        
        LocationRequest locationRequest = new LocationRequest.Builder(10000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build();
            
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    fusedLocationClient.removeLocationUpdates(this);
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    currentAddress = "Lat: " + currentLatitude + ", Lon: " + currentLongitude;
                    validateLocationWithApi(eventType, eventName);
                } else {
                    if (tvStatus != null) tvStatus.setText("Cannot get location. Please try again.");
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
                        String errorMsg = "You are NOT at the work location!";
                        if (tvStatus != null) tvStatus.setText(errorMsg);
                        Toast.makeText(KitchenActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
                
                @Override
                public void onFailure(Call<LocationResponse> call, Throwable t) {
                    if (tvStatus != null) tvStatus.setText("Location validation failed: " + t.getMessage());
                    Toast.makeText(KitchenActivity.this, "Location validation failed", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void recordAttendance(String eventType, String eventName) {
        AttendanceRequest request = new AttendanceRequest(
            session.getUserId(), session.getUsername(), session.getFullName(),
            department, eventType, eventName, currentLatitude, currentLongitude, currentAddress
        );
        
        ApiClient.getApiService().recordAttendance(request)
            .enqueue(new Callback<AttendanceResponse>() {
                @Override
                public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String msg = eventName + " recorded successfully";
                        if (tvStatus != null) tvStatus.setText(msg);
                        Toast.makeText(KitchenActivity.this, eventName + " Success!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (tvStatus != null) tvStatus.setText("Failed to record " + eventName);
                        Toast.makeText(KitchenActivity.this, "Failed to record!", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                    if (tvStatus != null) tvStatus.setText("Network error: " + t.getMessage());
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
                Toast.makeText(this, "Location permission required to sign in/out!", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void logout() {
        session.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
