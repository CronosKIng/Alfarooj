package com.alfarooj.timetable.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
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
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryActivity extends BaseActivity {
    
    private TextView tvWelcome, tvStatus;
    private SessionManager session;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private double currentLatitude = 0, currentLongitude = 0;
    private String pendingEventType = "", pendingEventName = "";
    private String pendingComment = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        
        session = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatus = findViewById(R.id.tvStatus);
        
        updateUI();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delivery_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_pickup) {
            checkLocation(TranslationHelper.translateTextDirect("pickup_order"), "Pickup Order");
            return true;
        } else if (id == R.id.action_dropoff) {
            checkLocation(TranslationHelper.translateTextDirect("dropoff_order"), "Dropoff Order");
            return true;
        } else if (id == R.id.action_signin) {
            checkLocation(TranslationHelper.translateTextDirect("sign_in"), "Sign In");
            return true;
        } else if (id == R.id.action_signout) {
            checkLocation(TranslationHelper.translateTextDirect("sign_out"), "Sign Out");
            return true;
        } else if (id == R.id.action_breakin) {
            checkLocation(TranslationHelper.translateTextDirect("break_in"), "Break In");
            return true;
        } else if (id == R.id.action_breakout) {
            checkLocation(TranslationHelper.translateTextDirect("break_out"), "Break Out");
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_language) {
            showLanguageDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void updateUI() {
        String dept = session.getDepartment();
        String icon = "🚗";
        tvWelcome.setText("User: " + session.getFullName() + " (" + dept + ") " + icon);
        tvStatus.setText("Ready - Select action from menu");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void checkLocation(String eventType, String eventName) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, TranslationHelper.translateTextDirect("Location permission required!"), Toast.LENGTH_LONG).show();
            return;
        }
        pendingEventType = eventType;
        pendingEventName = eventName;

        if (eventType.equals(TranslationHelper.translateTextDirect("sign_in"))) {
            showLateDialog();
        } else {
            pendingComment = "";
            startLocationCheck();
        }
    }

    private void showLateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TranslationHelper.translateTextDirect("Are you late?"));
        builder.setMessage(TranslationHelper.translateTextDirect("Did you arrive late to work today?"));
        
        builder.setPositiveButton(TranslationHelper.translateTextDirect("Yes, I'm late"), (dialog, which) -> {
            showCommentDialog();
        });
        
        builder.setNegativeButton(TranslationHelper.translateTextDirect("No, I'm on time"), (dialog, which) -> {
            pendingComment = "";
            startLocationCheck();
        });
        
        builder.show();
    }

    private void showCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TranslationHelper.translateTextDirect("Reason for lateness"));

        final EditText input = new EditText(this);
        input.setHint(TranslationHelper.translateTextDirect("Enter your reason here..."));
        builder.setView(input);
        
        builder.setPositiveButton(TranslationHelper.translateTextDirect("Submit"), (dialog, which) -> {
            pendingComment = input.getText().toString().trim();
            startLocationCheck();
        });

        builder.setNegativeButton(TranslationHelper.translateTextDirect("Skip"), (dialog, which) -> {
            pendingComment = "";
            startLocationCheck();
        });

        builder.show();
    }

    private void startLocationCheck() {
        tvStatus.setText(TranslationHelper.translateTextDirect("Getting location..."));
        LocationRequest request = new LocationRequest.Builder(10000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();
        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result != null && result.getLastLocation() != null) {
                    fusedLocationClient.removeLocationUpdates(this);
                    currentLatitude = result.getLastLocation().getLatitude();
                    currentLongitude = result.getLastLocation().getLongitude();
                    validateLocation();
                } else {
                    tvStatus.setText(TranslationHelper.translateTextDirect("Location failed"));
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper());
    }

    private void validateLocation() {
        com.alfarooj.timetable.models.LocationRequest req = new com.alfarooj.timetable.models.LocationRequest(currentLatitude, currentLongitude);
        ApiClient.getApiService().validateLocation(req).enqueue(new Callback<LocationResponse>() {
            @Override
            public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isWithinLocation())
                    recordAttendance();
                else
                    tvStatus.setText(TranslationHelper.translateTextDirect("Not at work location!"));
            }
            
            @Override
            public void onFailure(Call<LocationResponse> call, Throwable t) {
                tvStatus.setText(TranslationHelper.translateTextDirect("Network error"));
            }
        });
    }

    private void recordAttendance() {
        String location = "Lat: " + currentLatitude + ", Lon: " + currentLongitude;
        AttendanceRequest request = new AttendanceRequest(session.getUserId(), session.getUsername(), session.getFullName(),
                session.getDepartment(), pendingEventType, pendingEventName, currentLatitude, currentLongitude, location);
        
        if (pendingEventType.equals(TranslationHelper.translateTextDirect("sign_in")) && !pendingComment.isEmpty()) {
            request.setComment(pendingComment);
        }

        if (pendingEventType.equals(TranslationHelper.translateTextDirect("pickup_order"))) {
            request.setOrderType(TranslationHelper.translateTextDirect("pickup"));
        } else if (pendingEventType.equals(TranslationHelper.translateTextDirect("dropoff_order"))) {
            request.setOrderType(TranslationHelper.translateTextDirect("dropoff"));
        }
        
        ApiClient.getApiService().recordAttendance(request).enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    tvStatus.setText(pendingEventName + " " + TranslationHelper.translateTextDirect("recorded"));
                    Toast.makeText(DeliveryActivity.this, pendingEventName + " " + TranslationHelper.translateTextDirect("Success!"), Toast.LENGTH_SHORT).show();
                    pendingComment = "";
                } else {
                    tvStatus.setText(TranslationHelper.translateTextDirect("Failed"));
                }
            }
            
            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                tvStatus.setText(TranslationHelper.translateTextDirect("Network error"));
            }
        });
    }

    private void logout() {
        session.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
