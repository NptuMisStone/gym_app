package com.NPTUMisStone.gym_app.User_And_Coach;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

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

import com.NPTUMisStone.gym_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.ImageHolder;
import com.mapbox.maps.MapView;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

public class Contact extends AppCompatActivity {
    MapView mapView;
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
        floatingActionButton.hide();
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