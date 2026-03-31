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

    // AL FAROOJ AL SHAMI RESTAURANT – SHARJAH, UAE (MUWAILEH)
    private static final double WORK_LATITUDE = 25.360722;
    private static final double WORK_LONGITUDE = 55.422792;
    private static final float ALLOWED_RADIUS_METERS = 100;

    private Context context;
    private FusedLocationProviderClient fusedClient;

    public interface LocationResultCallback {
        void onLocationSuccess(double lat, double lon, String address);
        void onLocationFailed(String error);
    }

    public LocationHelper(Context ctx) {
        this.context = ctx;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(ctx);
    }

    public void getCurrentLocation(LocationResultCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationFailed("Ruhusa ya eneo haijatolewa. Tafadhali iwashe.");
            return;
        }

        LocationRequest request = new LocationRequest.Builder(8000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null || result.getLastLocation() == null) {
                    callback.onLocationFailed("Haiwezi kupata eneo lako. Jaribu tena.");
                    return;
                }
                Location loc = result.getLastLocation();
                fusedClient.removeLocationUpdates(this);
                callback.onLocationSuccess(loc.getLatitude(), loc.getLongitude(),
                        "Lat: " + loc.getLatitude() + ", Lon: " + loc.getLongitude());
            }
        };
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    public boolean isWithinWorkLocation(double lat, double lon) {
        float[] dist = new float[1];
        Location.distanceBetween(lat, lon, WORK_LATITUDE, WORK_LONGITUDE, dist);
        return dist[0] <= ALLOWED_RADIUS_METERS;
    }

    public void checkLocationAndProceed(LocationResultCallback callback, Runnable onSuccess) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationFailed("Ruhusa ya eneo haijatolewa. Wezesha kwenye mipangilio.");
            return;
        }
        getCurrentLocation(new LocationResultCallback() {
            @Override
            public void onLocationSuccess(double lat, double lon, String addr) {
                if (isWithinWorkLocation(lat, lon)) {
                    onSuccess.run();
                } else {
                    callback.onLocationFailed("HUPO ENEO LA KAZI! Unaruhusiwi kusign. Tafadhali nenda Al Farooj Al Shami Restaurant (Sharjah, UAE).");
                }
            }
            @Override
            public void onLocationFailed(String err) {
                callback.onLocationFailed(err);
            }
        });
    }
}
