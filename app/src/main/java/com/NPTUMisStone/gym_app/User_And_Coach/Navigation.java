package com.NPTUMisStone.gym_app.User_And_Coach;

/*import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;*/
import androidx.appcompat.app.AppCompatActivity;/*
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImpl;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;*/

public class Navigation extends AppCompatActivity {
    /*MapView mapView;
    Button setRouteButton;
    FloatingActionButton floatingActionButton;
    NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private MapboxRouteLineView mapboxRouteLineView;
    private MapboxRouteLineApi mapboxRouteLineApi;
    LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewRawLocation(@NonNull android.location.Location location) {

        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(location, locationMatcherResult.getKeyPoints(),null, null);
            if(focusLocation)
                updateCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()), location.getBearing());
        }
    };
    RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            mapboxRouteLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), new MapboxNavigationConsumer<Expected<RouteLineError, RouteSetValue>>() {
                @Override
                public void accept(Expected<RouteLineError, RouteSetValue> routeLineErrorRouteSetValueExpected) {
                    Style style = mapView.getMapboxMap().getStyle();
                    if(style != null) {
                        mapboxRouteLineView.renderRouteDrawData(style, routeLineErrorRouteSetValueExpected);
                    }
                }
            });
        }
    };
    private boolean focusLocation = true;
    MapboxNavigation mapboxNavigation;
    private void updateCamera(Point point, double bearing) {
        MapAnimationOptions mapAnimationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(15.0).bearing(bearing).pitch(45.0)
                .padding(new EdgeInsets(1000.0,0.0,0.0,0.0)).build();
        getCamera(mapView).easeTo(cameraOptions, mapAnimationOptions, null);
    }
    OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
        }

        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            focusLocation = false;
            getGestures(mapView).removeOnMoveListener(this);
            floatingActionButton.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }
    };
    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if(isGranted) {
            Toast.makeText(this, "Permission granted! Please restart the app", Toast.LENGTH_SHORT).show();
        }
    });
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
        /*mapView = findViewById(R.id.mapView);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        setRouteButton = findViewById(R.id.setRouteButton);*//*
        MapboxRouteLineOptions mapboxRouteLineOptions = new MapboxRouteLineOptions.Builder(this)
                .withRouteLineBelowLayerId("road-label-navigation").build();
        mapboxRouteLineView = new MapboxRouteLineView(mapboxRouteLineOptions);
        mapboxRouteLineApi = new MapboxRouteLineApi(mapboxRouteLineOptions);
        NavigationOptions navigationOptions = new NavigationOptions.Builder(this).accessToken(getString(R.string.mapbox_access_token)).build();
        MapboxNavigationApp.setup(navigationOptions);
        mapboxNavigation = new MapboxNavigation(navigationOptions);
        mapboxNavigation.registerRoutesObserver(routesObserver);
        mapboxNavigation.registerLocationObserver(locationObserver);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if(checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        if(checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }else {
            mapboxNavigation.startTripSession();
        }
        floatingActionButton.hide();
        LocationComponentPlugin locationComponentPlugin = mapView.getPlugin(Plugin.MAPBOX_LOCATION_COMPONENT_PLUGIN_ID);
        getGestures(mapView).addOnMoveListener(onMoveListener);
        setRouteButton.setOnClickListener(v -> Toast
                .makeText(this, "Please select a location in map", Toast.LENGTH_SHORT).show());
        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE, style -> {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
            locationComponentPlugin.setEnabled(true);
            locationComponentPlugin.setLocationProvider(navigationLocationProvider);
            getGestures(mapView).addOnMoveListener(onMoveListener);
            locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                @Override
                public Unit invoke(LocationComponentSettings locationComponentSettings) {
                    locationComponentSettings.setEnabled(true);
                    locationComponentSettings.setPulsingEnabled(true);
                    return null;
                }
            });
        });
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager("",mapView);
        addOnMapClickListener(mapView, point -> {
            pointAnnotationManager.deleteAll();
            PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withPoint(point).withIconImage(bitmap).withPoint(point);
            pointAnnotationManager.create(pointAnnotationOptions);
            setRouteButton.setOnClickListener(v -> {
                fetchRoute(point);
            });
            return true;
        });
        floatingActionButton.setOnClickListener(v -> {
            focusLocation = true;
            getGestures(mapView).addOnMoveListener(onMoveListener);
            floatingActionButton.hide();
        });
    }
    @SuppressLint("MissingPermission")
    private void fetchRoute(Point point) {
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(MainActivity.this);
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();
                setRoute.setEnabled(false);
                setRoute.setText("Fetching route...");
                RouteOptions.Builder builder = RouteOptions.builder();
                Point origin = Point.fromLngLat(Objects.requireNonNull(location).getLongitude(), location.getLatitude());
                builder.coordinatesList(Arrays.asList(origin, point));
                builder.alternatives(false);
                builder.profile(DirectionsCriteria.PROFILE_DRIVING);
                builder.bearingsList(Arrays.asList(Bearing.builder().angle(location.getBearing()).degrees(45.0).build(), null));
                applyDefaultNavigationOptions(builder);

                mapboxNavigation.requestRoutes(builder.build(), new NavigationRouterCallback() {
                    @Override
                    public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
                        mapboxNavigation.setNavigationRoutes(list);
                        focusLocationBtn.performClick();
                        setRoute.setEnabled(true);
                        setRoute.setText("Set route");
                    }

                    @Override
                    public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                        setRoute.setEnabled(true);
                        setRoute.setText("Set route");
                        Toast.makeText(MainActivity.this, "Route request failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {

                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }
*/}