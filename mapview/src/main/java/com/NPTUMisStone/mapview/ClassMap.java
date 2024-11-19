package com.NPTUMisStone.mapview;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ClassMap extends AppCompatActivity {
    private MapView mapView;

    FloatingActionButton floatingActionButton;
    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
    });
    OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = accuracyRadius -> mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(accuracyRadius).build());
    OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = position -> {
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(position).zoom(17.0).build());
        getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(position));
    };
    OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            getGestures(mapView).removeOnMoveListener(onMoveListener);
            floatingActionButton.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
        }
    };

    //【Mapbox Docs - 地理編碼API】
    // ：https://docs.mapbox.com/api/search/geocoding/
    //【Retrofit 2 - URL Query Parameter】
    // ：https://stackoverflow.com/questions/36730086/retrofit-2-url-query-parameter
    //(可參考)地理編碼資源分享
    // ：https://medium.com/geopainter/geocoding%E8%B3%87%E6%BA%90%E5%88%86%E4%BA%AB-2e7614aba49
    interface GeocodingService {
        @GET("forward")
        Call<ResponseBody> getCoordinates(@Query("q") String address, @Query("access_token") String accessToken);
    }

    private Retrofit retrofitInstance;

    //How to add marker using Java in v10? #916：https://github.com/mapbox/mapbox-maps-android/issues/916
    //Add marker to Mapbox in Java Android：https://stackoverflow.com/questions/78358830/add-marker-to-mapbox-in-java-android
    //How to add a single annotation / marker with Mapbox Maps SDK for Android in Java：https://stackoverflow.com/questions/72154628/how-to-add-a-single-annotation-marker-with-mapbox-maps-sdk-for-android-in-java
    //Mapbox Docs - Add Point Annotations：https://docs.mapbox.com/android/maps/examples/add-point-annotations/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_and_coach_class_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        /*mapView = findViewById(R.id.Navigation_mapView);
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> addMarkers());
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(Point.fromLngLat(121, 23))
                .zoom(10.0).build());*/
        retrofitInstance = new Retrofit.Builder().baseUrl("https://api.mapbox.com/search/geocode/v6/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        init_button();
        init_map((String[][]) getIntent().getSerializableExtra("addresses"));
    }//.withIconImage("marker-15")

    private void init_button() {
        findViewById(R.id.Navigation_backButton).setOnClickListener(v -> finish());
        findViewById(R.id.Navigation_leftButton).setOnClickListener(v -> mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(mapView.getMapboxMap().getCameraState().getZoom() - 1).build()));
        findViewById(R.id.Navigation_rightButton).setOnClickListener(v -> mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(mapView.getMapboxMap().getCameraState().getZoom() + 1).build()));
    }

    private LocationEngine locationEngine;

    private void init_map(String[][] addresses) {
        mapView = findViewById(R.id.Navigation_mapView);
        floatingActionButton = findViewById(R.id.Navigation_floatingActionButton);
        floatingActionButton.hide();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED)
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        mapView.getMapboxMap().loadStyleUri(getString(R.string.mapbox_style_url), style -> {
            if (addresses != null) addMarkers(addresses);
            else new AlertDialog.Builder(ClassMap.this).setTitle("沒有任何課程，請確認是否由主程式進入").setPositiveButton("確定", (dialog, which) -> finish()).create().show();
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(15.0).build());
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
            locationComponentPlugin.setEnabled(true);
            getGestures(mapView).addOnMoveListener(onMoveListener);
            floatingActionButton.setOnClickListener(v -> {
                locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                getGestures(mapView).addOnMoveListener(onMoveListener);
                floatingActionButton.hide();
            });

            // Initialize LocationEngine
            locationEngine = LocationEngineProvider.getBestLocationEngine(this);
            locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    if (result.getLastLocation() != null) {
                        // Use the last known location
                        Point lastKnownLocation = Point.fromLngLat(result.getLastLocation().getLongitude(), result.getLastLocation().getLatitude());
                        mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(lastKnownLocation).zoom(17.0).build());
                    } else {
                        // If location is not available, query Minsheng Dormitory location
                        queryMinshengDormitoryLocation();
                    }
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("LocationEngine", "Error getting last location", exception);
                    // If location is not available, query Minsheng Dormitory location
                    queryMinshengDormitoryLocation();
                }
            });
        });

    }

    private void queryMinshengDormitoryLocation() {
        GeocodingService geocodingService = retrofitInstance.create(GeocodingService.class);
        geocodingService.getCoordinates("Minsheng Dormitory", getString(R.string.mapbox_access_token)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                handleGeocodingResponse(response, "Minsheng Dormitory", "Minsheng Dormitory", null, -1);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("Geocoding", "Request failed for Minsheng Dormitory", t);
            }
        });
    }

    private void addMarkers(String[][] addresses) {
        AnnotationPlugin annotationAPI = AnnotationPluginImplKt.getAnnotations(mapView);
        PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationAPI, mapView);

        GeocodingService geocodingService = retrofitInstance.create(GeocodingService.class);

        for (String[] address : addresses) {
            final String ClassAddress = address[0];
            final String ClassName = address[1];
            geocodingService.getCoordinates(ClassAddress, getString(R.string.mapbox_access_token)).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    handleGeocodingResponse(response, ClassAddress, ClassName, pointAnnotationManager, Integer.parseInt(address[2]));
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e("Geocoding", "Request failed for address " + ClassAddress, t);
                }
            });
        }
    }

    private void handleGeocodingResponse(Response<ResponseBody> response, String ClassAddress, String ClassName, PointAnnotationManager pointAnnotationManager, int classID) {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            if (response.isSuccessful()) {
                try (ResponseBody body = responseBody) {
                    String responseBodyString = body.string();
                    Log.d("Geocoding", "Response for address " + ClassAddress + ": " + responseBodyString);
                    Point point = parseCoordinates(responseBodyString);
                    if (point != null) {
                        Log.d("Geocoding", "Parsed point for address " + ClassAddress + ": " + point);
                        addPointAnnotation(point, ClassName, pointAnnotationManager, classID);
                    } else {
                        Log.e("Geocoding", "Parsed point is null for address " + ClassAddress);
                    }
                } catch (Exception e) {
                    Log.e("Geocoding", "Error parsing response for address " + ClassAddress, e);
                }
            } else {
                Log.e("Geocoding", "Request failed with code: " + response.code() + " for address " + ClassAddress);
            }
        } else {
            Log.e("Geocoding", "Request failed with code: " + response.code() + " for address " + ClassAddress);
        }
    }

    private void addPointAnnotation(Point point, String ClassName, PointAnnotationManager pointAnnotationManager, int classID) {
        Drawable drawable = AppCompatResources.getDrawable(ClassMap.this, R.drawable.baseline_location_on_24);
        Bitmap bitmap = drawableToBitmap(drawable);
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(point).withIconImage(bitmap).withTextAnchor(TextAnchor.LEFT)
                .withTextField(ClassName).withTextOffset(Arrays.asList(1.0, 0.0));
        PointAnnotation pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions);
        pointAnnotationManager.addClickListener(annotation -> {
            if (annotation.getId() == pointAnnotation.getId()) {
                AlertDialog builder = new AlertDialog.Builder(ClassMap.this)
                        .setTitle("課程地址").setView(R.layout.coach_class_item)
                        .setPositiveButton("查看更多資訊", (dialog, which) -> {
                            if (classID != -1) {
                                setResult(RESULT_OK, getIntent().putExtra("看更多課程ID", classID));
                                finish();
                            } else Toast.makeText(ClassMap.this, "無法取得課程ID", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("導航", (dialog, which) -> startActivity(new Intent(this,Navigation.class))).show();
                builder.create();
                return true;
            }
            return false;
        });
    }

    private Point parseCoordinates(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray features = jsonObject.getJSONArray("features");
            if (features.length() > 0) {
                JSONObject feature = features.getJSONObject(0);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                double longitude = coordinates.getDouble(0);
                double latitude = coordinates.getDouble(1);
                return Point.fromLngLat(longitude, latitude);
            }
        } catch (Exception e) {
            Log.e("Geocoding", "Error parsing coordinates", e);
        }
        Toast.makeText(this, "解析座標失敗", Toast.LENGTH_SHORT).show();
        return null;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) return ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}