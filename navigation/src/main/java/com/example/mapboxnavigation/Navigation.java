package com.example.mapboxnavigation;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.LocationPuck2D;
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

public class Navigation extends AppCompatActivity {
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
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {}
    };
    String[][] addresses = {
            {"屏東大學","課程1"},
            {"台中","課程2"},
            {"花蓮","課程3"}
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
        setContentView(R.layout.user_and_coach_navigation);
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
        //TODO: 接收課程及地址資料
        retrofitInstance = new Retrofit.Builder()
                .baseUrl("https://api.mapbox.com/search/geocode/v6/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        init_button();
        init_map();
    }//.withIconImage("marker-15")
    private void init_button(){
        findViewById(R.id.Navigation_backButton).setOnClickListener(v -> finish());
        findViewById(R.id.Navigation_leftButton).setOnClickListener(v -> mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(mapView.getMapboxMap().getCameraState().getZoom() - 1).build()));
        findViewById(R.id.Navigation_rightButton).setOnClickListener(v -> mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(mapView.getMapboxMap().getCameraState().getZoom() + 1).build()));
    }

    private void init_map(){
        mapView = findViewById(R.id.Navigation_mapView);
        floatingActionButton = findViewById(R.id.Navigation_floatingActionButton);
        floatingActionButton.hide();
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED)
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        mapView.getMapboxMap().loadStyleUri(getString(R.string.mapbox_style_url), style -> {
            addMarkers();
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(15.0).build());
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
            locationComponentPlugin.setEnabled(true);
            LocationPuck2D locationPuck2D = new LocationPuck2D();

            Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_location_on_24);
            Bitmap bitmap = drawableToBitmap(drawable);
            //TODO 確認是否移除有問題
            /*locationPuck2D.setBearingImage(ImageHolder.from(bitmap));
            locationComponentPlugin.setLocationPuck(locationPuck2D);
            locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
*/
            getGestures(mapView).addOnMoveListener(onMoveListener);
            floatingActionButton.setOnClickListener(v -> {
                locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                getGestures(mapView).addOnMoveListener(onMoveListener);
                floatingActionButton.hide();
            });
        });
    }
    private void addMarkers() {
        AnnotationPlugin annotationAPI = AnnotationPluginImplKt.getAnnotations(mapView);
        PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationAPI, mapView);

        GeocodingService geocodingService = retrofitInstance.create(GeocodingService.class);

        for (String[] address : addresses) {
            final String ClassAddress = address[0];
            final String ClassName = address[1];
            geocodingService.getCoordinates(ClassAddress, getString(R.string.mapbox_access_token)).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    handleGeocodingResponse(response, ClassAddress, ClassName, pointAnnotationManager);
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e("Geocoding", "Request failed for address " + ClassAddress, t);
                }
            });
        }
    }

    private void handleGeocodingResponse(Response<ResponseBody> response, String ClassAddress, String ClassName, PointAnnotationManager pointAnnotationManager) {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            if (response.isSuccessful()) {
                try (ResponseBody body = responseBody) {
                    String responseBodyString = body.string();
                    Log.d("Geocoding", "Response for address " + ClassAddress + ": " + responseBodyString);
                    Point point = parseCoordinates(responseBodyString);
                    if (point != null) {
                        Log.d("Geocoding", "Parsed point for address " + ClassAddress + ": " + point);
                        addPointAnnotation(point, ClassName, pointAnnotationManager);
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

    private void addPointAnnotation(Point point, String ClassName, PointAnnotationManager pointAnnotationManager) {
        Drawable drawable = AppCompatResources.getDrawable(Navigation.this, R.drawable.baseline_location_on_24);
        Bitmap bitmap = drawableToBitmap(drawable);
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(bitmap)
                .withTextField(ClassName)
                .withTextAnchor(TextAnchor.LEFT)
                .withTextOffset(Arrays.asList(1.0, 0.0));
        PointAnnotation pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions);
        pointAnnotationManager.addClickListener(annotation -> {
            if (annotation.getId() == pointAnnotation.getId()) {
                Toast.makeText(Navigation.this, ClassName, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, longitude + " " + latitude, Toast.LENGTH_SHORT).show();
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