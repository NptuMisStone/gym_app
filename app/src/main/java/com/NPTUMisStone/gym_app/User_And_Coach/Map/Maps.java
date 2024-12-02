package com.NPTUMisStone.gym_app.User_And_Coach.Map;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

public class Maps extends AppCompatActivity implements OnMapReadyCallback {
    Connection MyConnection;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    LatLng currentLocation;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_and_coach_map_maps);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        findViewById(R.id.MapMaps_backButton).setOnClickListener(v -> finish());

        address = getIntent().getStringExtra("address");
        Toast.makeText(this, "Address: " + address, Toast.LENGTH_SHORT).show();
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST, renderer -> {});

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            initMap();
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                            if (address != null && !address.isEmpty()) {
                                geocodeAddress(address);
                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid address.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to get your location.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        mMap.setOnMarkerClickListener(marker -> {
            if (!Objects.requireNonNull(marker.getTitle()).isEmpty()) {
                new MapHelper(this, address).getCurrentLocation();
            }
            return true;
        });
    }


    private class FetchDirectionsTask extends AsyncTask<LatLng, Void, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(LatLng... params) {
            LatLng origin = params[0];
            LatLng destination = params[1];
            String apiKey = "AIzaSyDVic0YITkggqQsWVA4a3tG4MXC-iIvTeY"; // 請替換為你自己的API金鑰
            String url = "https://maps.googleapis.com/maps/api/directions/json?"
                    + "origin=" + origin.latitude + "," + origin.longitude
                    + "&destination=" + destination.latitude + "," + destination.longitude
                    + "&key=" + apiKey;
            try {
                String jsonResponse = getJsonResponse(url);
                if (jsonResponse != null) {
                    return parseJsonResponse(jsonResponse);
                }
            } catch (Exception e) {
                Log.e("MapActivity", "Error fetching directions: " + e.getMessage());
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<LatLng> routePoints) {
            if (!routePoints.isEmpty()) {
                List<LatLng> simplifiedRoutePoints = PolyUtil.simplify(routePoints, 0.1);
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(simplifiedRoutePoints).width(5).color(Color.RED);
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(Maps.this, "No routes found between the specified locations.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap();
            }
        }
    }

    private String getJsonResponse(String url) throws IOException {
        URL requestUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
        try (InputStream in = conn.getInputStream(); Scanner scanner = new Scanner(in)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : null;
        } finally {
            conn.disconnect();
        }
    }

    private List<LatLng> parseJsonResponse(String jsonResponse) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        if (jsonObject.has("routes") && jsonObject.getJSONArray("routes").length() > 0) {
            JSONObject route = jsonObject.getJSONArray("routes").getJSONObject(0);
            JSONObject polyline = route.getJSONObject("overview_polyline");
            String encodedPolyline = polyline.getString("points");
            return PolyUtil.decode(encodedPolyline);
        } else {
            throw new Exception("No routes found in JSON response.");
        }
    }

    private void geocodeAddress(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Marker myMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng).title("我家").snippet("我家")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                new FetchDirectionsTask().execute(latLng, currentLocation);
            } else {
                Toast.makeText(getApplicationContext(), "地址無效，請檢查輸入的地址。", Toast.LENGTH_SHORT).show();
                Log.e("MapActivity", "地址無效: " + address);
            }
        } catch (IOException e) {
            Log.e("MapActivity", "地理編碼錯誤: " + e.getMessage());
        }
    }
}