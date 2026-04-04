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

public class WaiterActivity extends BaseActivity {
    private Button btnSignIn, btnBreakStart, btnBreakEnd, btnSignOut, btnViewHistory, btnLogout;
    private TextView tvWelcome, tvStatus;
    private SessionManager session;
    private FusedLocationProviderClient fusedLocationClient;
    private String department = "waiter";
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private String currentAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiter);

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

        // Set welcome text without "WELCOME" prefix
        tvWelcome.setText("User: " + session.getFullName());
        
        translateUI();

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }

        btnSignIn.setOnClickListener(v -> checkLocationAndProceed("sign_in", "Sign In"));
        btnBreakStart.setOnClickListener(v -> checkLocationAndProceed("break_start", "Break Start"));
        btnBreakEnd.setOnClickListener(v -> checkLocationAndProceed("break_end", "Break End"));
        btnSignOut.setOnClickListener(v -> checkLocationAndProceed("sign_out", "Sign Out"));
        btnViewHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> logout());
    }
    
    private void translateUI() {
        String lang = TranslationHelper.getCurrentLanguage();
        
        // Translate status text
        if (lang.equals("en")) {
            tvStatus.setText("Ready");
            btnSignIn.setText("SIGN IN");
            btnBreakStart.setText("BREAK START");
            btnBreakEnd.setText("BREAK END");
            btnSignOut.setText("SIGN OUT");
            btnViewHistory.setText("HISTORY");
            btnLogout.setText("LOGOUT");
        } else {
            TranslationHelper.translateText("Ready", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { tvStatus.setText(translated); }
                @Override public void onError(String error) { tvStatus.setText("Ready"); }
            });
            TranslationHelper.translateText("SIGN IN", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { btnSignIn.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("BREAK START", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { btnBreakStart.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("BREAK END", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { btnBreakEnd.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("SIGN OUT", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { btnSignOut.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("HISTORY", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { btnViewHistory.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("LOGOUT", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { btnLogout.setText(translated); }
                @Override public void onError(String error) {}
            });
        }
    }
    
    private void checkLocationAndProceed(String eventType, String eventName) {
        // First check if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String msg = "Location permission required! Please enable location.";
            String lang = TranslationHelper.getCurrentLanguage();
            if (lang.equals("en")) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            } else {
                TranslationHelper.translateText(msg, new TranslationHelper.TranslationCallback() {
                    @Override public void onSuccess(String translated) { Toast.makeText(WaiterActivity.this, translated, Toast.LENGTH_LONG).show(); }
                    @Override public void onError(String error) { Toast.makeText(WaiterActivity.this, msg, Toast.LENGTH_LONG).show(); }
                });
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        
        // Show checking status
        String checkingMsg = "Checking location...";
        String lang = TranslationHelper.getCurrentLanguage();
        if (lang.equals("en")) {
            tvStatus.setText(checkingMsg);
        } else {
            TranslationHelper.translateText(checkingMsg, new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { tvStatus.setText(translated); }
                @Override public void onError(String error) { tvStatus.setText(checkingMsg); }
            });
        }
        
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
                    String errorMsg = "Cannot get location. Please try again.";
                    String lang = TranslationHelper.getCurrentLanguage();
                    if (lang.equals("en")) {
                        tvStatus.setText(errorMsg);
                        Toast.makeText(WaiterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                            @Override public void onSuccess(String translated) { 
                                tvStatus.setText(translated);
                                Toast.makeText(WaiterActivity.this, translated, Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(String error) { 
                                tvStatus.setText(errorMsg);
                                Toast.makeText(WaiterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
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
                        String errorMsg = "You are NOT at the work location! You must be at Al Farooj Al Shami Restaurant to sign in/out.";
                        String lang = TranslationHelper.getCurrentLanguage();
                        if (lang.equals("en")) {
                            tvStatus.setText(errorMsg);
                            Toast.makeText(WaiterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        } else {
                            TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                @Override public void onSuccess(String translated) { 
                                    tvStatus.setText(translated);
                                    Toast.makeText(WaiterActivity.this, translated, Toast.LENGTH_LONG).show();
                                }
                                @Override public void onError(String error) { 
                                    tvStatus.setText(errorMsg);
                                    Toast.makeText(WaiterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<LocationResponse> call, Throwable t) {
                    String errorMsg = "Location validation failed: " + t.getMessage();
                    String lang = TranslationHelper.getCurrentLanguage();
                    if (lang.equals("en")) {
                        tvStatus.setText(errorMsg);
                        Toast.makeText(WaiterActivity.this, "Location validation failed", Toast.LENGTH_SHORT).show();
                    } else {
                        TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                            @Override public void onSuccess(String translated) { tvStatus.setText(translated); }
                            @Override public void onError(String error) { tvStatus.setText(errorMsg); }
                        });
                    }
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
                        String successMsg = eventName + " recorded successfully!";
                        String lang = TranslationHelper.getCurrentLanguage();
                        if (lang.equals("en")) {
                            tvStatus.setText(successMsg);
                            Toast.makeText(WaiterActivity.this, eventName + " Success!", Toast.LENGTH_SHORT).show();
                        } else {
                            TranslationHelper.translateText(successMsg, new TranslationHelper.TranslationCallback() {
                                @Override public void onSuccess(String translated) { 
                                    tvStatus.setText(translated);
                                    Toast.makeText(WaiterActivity.this, translated, Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onError(String error) { 
                                    tvStatus.setText(successMsg);
                                    Toast.makeText(WaiterActivity.this, eventName + " Success!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        String errorMsg = "Failed to record " + eventName;
                        String lang = TranslationHelper.getCurrentLanguage();
                        if (lang.equals("en")) {
                            tvStatus.setText(errorMsg);
                            Toast.makeText(WaiterActivity.this, "Failed to record!", Toast.LENGTH_SHORT).show();
                        } else {
                            TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                @Override public void onSuccess(String translated) { tvStatus.setText(translated); }
                                @Override public void onError(String error) { tvStatus.setText(errorMsg); }
                            });
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                    String errorMsg = "Network error: " + t.getMessage();
                    String lang = TranslationHelper.getCurrentLanguage();
                    if (lang.equals("en")) {
                        tvStatus.setText(errorMsg);
                        Toast.makeText(WaiterActivity.this, "Network error!", Toast.LENGTH_SHORT).show();
                    } else {
                        TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                            @Override public void onSuccess(String translated) { tvStatus.setText(translated); }
                            @Override public void onError(String error) { tvStatus.setText(errorMsg); }
                        });
                    }
                }
            });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String msg = "Location permission granted";
                String lang = TranslationHelper.getCurrentLanguage();
                if (lang.equals("en")) {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                } else {
                    TranslationHelper.translateText(msg, new TranslationHelper.TranslationCallback() {
                        @Override public void onSuccess(String translated) { Toast.makeText(WaiterActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                        @Override public void onError(String error) { Toast.makeText(WaiterActivity.this, msg, Toast.LENGTH_SHORT).show(); }
                    });
                }
            } else {
                String msg = "Location permission required to sign in/out!";
                String lang = TranslationHelper.getCurrentLanguage();
                if (lang.equals("en")) {
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                } else {
                    TranslationHelper.translateText(msg, new TranslationHelper.TranslationCallback() {
                        @Override public void onSuccess(String translated) { Toast.makeText(WaiterActivity.this, translated, Toast.LENGTH_LONG).show(); }
                        @Override public void onError(String error) { Toast.makeText(WaiterActivity.this, msg, Toast.LENGTH_LONG).show(); }
                    });
                }
            }
        }
    }
    
    private void logout() {
        session.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
