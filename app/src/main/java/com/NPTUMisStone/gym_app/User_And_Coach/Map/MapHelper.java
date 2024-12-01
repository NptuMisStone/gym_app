package com.NPTUMisStone.gym_app.User_And_Coach.Map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MapHelper {
    private final Context context;
    private final Connection connection;
    private final int classID;

    public MapHelper(Context context, Connection connection, int classID) {
        this.context = context;
        this.connection = connection;
        this.classID = classID;
    }

    //【How to Get Current Location Google Map SDK in Android】
    // ：https://mrappbuilder.medium.com/how-to-get-current-location-google-map-sdk-in-android-993638d17bd1
    public void getCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (checkLocationPermissions()) {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                openGoogleMaps(location.getLatitude(), location.getLongitude());
                            } else {
                                Log.e("MapHelper", "Location data is unavailable");
                            }
                        })
                        .addOnFailureListener(e -> Log.e("MapHelper", "Failed to get location", e));
            } catch (SecurityException e) {
                Log.e("MapHelper", "Location permission not granted", e);
            }
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermissions() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions((Activity) context, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 1);
    }

    //【How to implement Google Map Navigation in Android】
    // ：https://www.youtube.com/watch?v=WiMa7nh7rF4
    private void openGoogleMaps(double latitude, double longitude) {
        String currentLocation = latitude + "," + longitude;
        executeQuery(rs -> {
            try {
                if (rs.next()) {
                    String address = rs.getString("縣市") + rs.getString("行政區") + rs.getString("顯示地點地址");
                    Uri uri = Uri.parse("https://www.google.com/maps/dir/" + currentLocation + "/" + address);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, uri)); // No Google Maps installed
                    }
                }
            } catch (SQLException e) {
                Log.e("MapHelper", "SQL error", e);
            }
        });
    }

    private void executeQuery(ResultSetHandler handler) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 縣市,行政區,顯示地點地址 FROM [健身教練課程-有排課的] WHERE 課程編號 = ?")) {
            statement.setInt(1, classID);
            try (ResultSet rs = statement.executeQuery()) {
                handler.handle(rs);
            }
        } catch (SQLException e) {
            Log.e("MapHelper", "SQL error", e);
        }
    }

    @FunctionalInterface
    private interface ResultSetHandler {
        void handle(ResultSet rs) throws SQLException;
    }
}