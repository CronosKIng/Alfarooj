package com.alfarooj.timetable.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
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
import com.alfarooj.timetable.utils.LanguageUtils;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends BaseActivity {
    private Button btnSignIn, btnSignOut, btnViewHistory, btnLogout;
    private TextView tvWelcome, tvStatus;
    private SessionManager session;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    @Override
    protected void attachBaseContext(Context newBase) {
        LanguageUtils.applyLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        
        session = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatus = findViewById(R.id.tvStatus);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnLogout = findViewById(R.id.btnLogout);
        
        updateUI();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
        
        btnSignIn.setOnClickListener(v -> checkLocationAndProceed(TranslationHelper.translateTextDirect("sign_in"), TranslationHelper.translateTextDirect("Sign In")));
        btnSignOut.setOnClickListener(v -> checkLocationAndProceed(TranslationHelper.translateTextDirect("sign_out"), TranslationHelper.translateTextDirect("Sign Out")));
        btnViewHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> logout());
    }

    private void updateUI() {
        String department = session.getDepartment();
        String deptDisplay = department != null ? TranslationHelper.translateTextDirect(department) : "";
        tvWelcome.setText(TranslationHelper.translateTextDirect("User: ") + session.getFullName() + " (" + deptDisplay + ")");
        
        btnSignIn.setText(TranslationHelper.translateTextDirect("SIGN IN"));
        btnSignOut.setText(TranslationHelper.translateTextDirect("SIGN OUT"));
        btnViewHistory.setText(TranslationHelper.translateTextDirect("VIEW HISTORY"));
        btnLogout.setText(TranslationHelper.translateTextDirect("LOGOUT"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void checkLocationAndProceed(String eventType, String eventName) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, TranslationHelper.translateTextDirect("Location permission required!"), Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        
        if (eventType.equals(TranslationHelper.translateTextDirect("sign_in"))) {
            showLateDialog(eventType, eventName);
        } else {
            getLocationAndRecord(eventType, eventName, "");
        }
    }
    
    private void showLateDialog(String eventType, String eventName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TranslationHelper.translateTextDirect("Are you late?"));
        builder.setMessage(TranslationHelper.translateTextDirect("Did you arrive late to work today?"));
        
        builder.setPositiveButton(TranslationHelper.translateTextDirect("Yes, I'm late"), (dialog, which) -> {
            showCommentDialog(eventType, eventName);
        });
        
        builder.setNegativeButton(TranslationHelper.translateTextDirect("No, I'm on time"), (dialog, which) -> {
            getLocationAndRecord(eventType, eventName, "");
        });
        
        builder.show();
    }
    
    private void showCommentDialog(String eventType, String eventName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TranslationHelper.translateTextDirect("Reason for lateness"));
        
        EditText input = new EditText(this);
        input.setHint(TranslationHelper.translateTextDirect("Enter your reason here..."));
        builder.setView(input);
        
        builder.setPositiveButton(TranslationHelper.translateTextDirect("Submit"), (dialog, which) -> {
            String comment = input.getText().toString().trim();
            getLocationAndRecord(eventType, eventName, comment);
        });
        
        builder.setNegativeButton(TranslationHelper.translateTextDirect("Skip"), (dialog, which) -> {
            getLocationAndRecord(eventType, eventName, "");
        });
        
        builder.show();
    }

    private void getLocationAndRecord(String eventType, String eventName, String comment) {
        tvStatus.setText(TranslationHelper.translateTextDirect("Getting location..."));
        
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
                    validateLocationWithApi(eventType, eventName, comment);
                } else {
                    tvStatus.setText(TranslationHelper.translateTextDirect("Cannot get location. Try again."));
                    Toast.makeText(UserActivity.this, TranslationHelper.translateTextDirect("Cannot get location"), Toast.LENGTH_SHORT).show();
                }
            }
        };
        
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void validateLocationWithApi(String eventType, String eventName, String comment) {
        com.alfarooj.timetable.models.LocationRequest locationRequest =
            new com.alfarooj.timetable.models.LocationRequest(currentLatitude, currentLongitude);

        ApiClient.getApiService().validateLocation(locationRequest)
            .enqueue(new Callback<LocationResponse>() {
                @Override
                public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isWithinLocation()) {
                        recordAttendance(eventType, eventName, comment);
                    } else {
                        tvStatus.setText(TranslationHelper.translateTextDirect("You are NOT at the work location!"));
                        Toast.makeText(UserActivity.this, TranslationHelper.translateTextDirect("You are NOT at the work location!"), Toast.LENGTH_LONG).show();
                    }
                }
                
                @Override
                public void onFailure(Call<LocationResponse> call, Throwable t) {
                    tvStatus.setText(TranslationHelper.translateTextDirect("Location validation failed"));
                    Toast.makeText(UserActivity.this, TranslationHelper.translateTextDirect("Location validation failed"), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void recordAttendance(String eventType, String eventName, String comment) {
        String department = session.getDepartment();
        String location = "Lat: " + currentLatitude + ", Lon: " + currentLongitude;
        
        AttendanceRequest request = new AttendanceRequest(
            session.getUserId(), session.getUsername(), session.getFullName(),
            department, eventType, eventName, currentLatitude, currentLongitude, location
        );
        
        if (!comment.isEmpty()) {
            request.setComment(comment);
        }
        
        ApiClient.getApiService().recordAttendance(request)
            .enqueue(new Callback<AttendanceResponse>() {
                @Override
                public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        tvStatus.setText(eventName + " " + TranslationHelper.translateTextDirect("recorded!"));
                        Toast.makeText(UserActivity.this, eventName + " " + TranslationHelper.translateTextDirect("Success!"), Toast.LENGTH_SHORT).show();
                    } else {
                        tvStatus.setText(TranslationHelper.translateTextDirect("Failed to record"));
                        Toast.makeText(UserActivity.this, TranslationHelper.translateTextDirect("Failed to record!"), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                    tvStatus.setText(TranslationHelper.translateTextDirect("Network error"));
                    Toast.makeText(UserActivity.this, TranslationHelper.translateTextDirect("Network error!"), Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, TranslationHelper.translateTextDirect("Location permission granted"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, TranslationHelper.translateTextDirect("Location permission required!"), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void logout() {
        session.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
