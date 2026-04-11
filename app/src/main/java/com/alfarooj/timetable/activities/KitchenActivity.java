package com.alfarooj.timetable.activities;

import android.Manifest;
import android.app.AlertDialog;
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
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KitchenActivity extends BaseActivity {
    private Button btnSignIn, btnSignOut, btnBreakIn, btnBreakOut, btnViewHistory, btnLogout;
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
        setContentView(R.layout.activity_kitchen);
        session = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatus = findViewById(R.id.tvStatus);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnBreakIn = findViewById(R.id.btnBreakIn);
        btnBreakOut = findViewById(R.id.btnBreakOut);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnLogout = findViewById(R.id.btnLogout);

        updateUI();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }

        btnSignIn.setOnClickListener(v -> checkLocation(TranslationHelper.translateTextDirect("sign_in"), TranslationHelper.translateTextDirect("Sign In")));
        btnSignOut.setOnClickListener(v -> checkLocation(TranslationHelper.translateTextDirect("sign_out"), TranslationHelper.translateTextDirect("Sign Out")));
        btnBreakIn.setOnClickListener(v -> checkLocation(TranslationHelper.translateTextDirect("break_in"), TranslationHelper.translateTextDirect("Break In")));
        btnBreakOut.setOnClickListener(v -> checkLocation(TranslationHelper.translateTextDirect("break_out"), TranslationHelper.translateTextDirect("Break Out")));
        btnViewHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> logout());
    }

    private void updateUI() {
        String dept = session.getDepartment();
        String icon = "🧑‍🍳";
        tvWelcome.setText(TranslationHelper.translateTextDirect("User: ") + session.getFullName() + " (" + TranslationHelper.translateTextDirect(dept) + ") " + icon);
        btnSignIn.setText(TranslationHelper.translateTextDirect("SIGN IN"));
        btnSignOut.setText(TranslationHelper.translateTextDirect("SIGN OUT"));
        btnBreakIn.setText(TranslationHelper.translateTextDirect("BREAK IN"));
        btnBreakOut.setText(TranslationHelper.translateTextDirect("BREAK OUT"));
        btnViewHistory.setText(TranslationHelper.translateTextDirect("VIEW HISTORY"));
        btnLogout.setText(TranslationHelper.translateTextDirect("LOGOUT"));
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

        ApiClient.getApiService().recordAttendance(request).enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    tvStatus.setText(pendingEventName + " " + TranslationHelper.translateTextDirect("recorded"));
                    Toast.makeText(KitchenActivity.this, pendingEventName + " " + TranslationHelper.translateTextDirect("Success!"), Toast.LENGTH_SHORT).show();
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
