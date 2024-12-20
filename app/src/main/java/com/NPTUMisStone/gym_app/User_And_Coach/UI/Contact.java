package com.NPTUMisStone.gym_app.User_And_Coach.UI;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.ImageHolder;
import com.mapbox.maps.MapView;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;

public class Contact extends AppCompatActivity {
    MapView mapView;
    FloatingActionButton floatingActionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_and_coach_contact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init_button();
        init_map();
    }
    private void init_button(){
        findViewById(R.id.Contact_backButton).setOnClickListener(v -> finish());
        findViewById(R.id.Contact_questionButton).setOnClickListener(v -> startActivity(new Intent(this, AIInteractive.class)));
        findViewById(R.id.Contact_leftButton).setOnClickListener(v -> mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(mapView.getMapboxMap().getCameraState().getZoom() - 1).build()));
        findViewById(R.id.Contact_rightButton).setOnClickListener(v -> mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(mapView.getMapboxMap().getCameraState().getZoom() + 1).build()));
    }
    private void init_map(){
        mapView = findViewById(R.id.Contact_mapView);
        floatingActionButton = findViewById(R.id.Contact_floatingActionButton);
        /*floatingActionButton.hide();
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED)
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        mapView.getMapboxMap().loadStyle(getString(R.string.mapbox_style_url), style -> {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(15.0).build());
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
            locationComponentPlugin.setEnabled(true);
            LocationPuck2D locationPuck2D = new LocationPuck2D();

            Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_location_on_24);
            Bitmap bitmap = drawableToBitmap(drawable);

            locationPuck2D.setBearingImage(ImageHolder.from(bitmap));
            locationComponentPlugin.setLocationPuck(locationPuck2D);
            locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);

            getGestures(mapView).addOnMoveListener(onMoveListener);
            floatingActionButton.setOnClickListener(v -> {
                locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                getGestures(mapView).addOnMoveListener(onMoveListener);
                floatingActionButton.hide();
            });
        });*/
        mapView.getMapboxMap().loadStyle(getString(R.string.mapbox_style_url), style -> {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .center(Point.  fromLngLat(120.5112317, 22.6573704))
                    .zoom(16.0).build());
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
            locationComponentPlugin.setEnabled(true);
            LocationPuck2D locationPuck2D = new LocationPuck2D();

            Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.all_ic_location);
            Bitmap bitmap = drawableToBitmap(drawable);
            locationPuck2D.setBearingImage(ImageHolder.from(bitmap));
            locationComponentPlugin.setLocationPuck(locationPuck2D);
            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(Point.fromLngLat(120.5112317, 22.6573704)));
        });
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