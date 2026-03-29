package com.alfarooj.timetable.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationHelper {
    private static final double TARGET_LATITUDE = -6.160000;
    private static final double TARGET_LONGITUDE = 35.740000;
    private static final float ALLOWED_RADIUS_METERS = 100;
    
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    
    public interface LocationResultCallback {
        void onLocationSuccess(double latitude, double longitude, String address);
        void onLocationFailed(String error);
    }
    
    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    
    public void getCurrentLocation(LocationResultCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationFailed("Hakuna ruhusa ya location");
            return;
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
                    callback.onLocationSuccess(location.getLatitude(), location.getLongitude(), "Location: " + location.getLatitude() + ", " + location.getLongitude());
                } else {
                    callback.onLocationFailed("Haiwezi kupata location");
                }
            }
        };
        
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    
    public boolean isWithinWorkLocation(double latitude, double longitude) {
        float[] results = new float[1];
        Location.distanceBetween(latitude, longitude, TARGET_LATITUDE, TARGET_LONGITUDE, results);
        return results[0] <= ALLOWED_RADIUS_METERS;
    }
    
    public void checkLocationAndProceed(LocationResultCallback callback, Runnable onSuccess) {
        getCurrentLocation(new LocationResultCallback() {
            @Override
            public void onLocationSuccess(double latitude, double longitude, String address) {
                if (isWithinWorkLocation(latitude, longitude)) {
                    onSuccess.run();
                } else {
                    callback.onLocationFailed("Hauruhusiwi kusign! Hakikisha uko eneo la kazi.");
                }
            }
            
            @Override
            public void onLocationFailed(String error) {
                callback.onLocationFailed(error);
            }
        });
    }
}
