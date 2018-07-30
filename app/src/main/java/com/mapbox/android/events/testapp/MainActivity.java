package com.mapbox.android.events.testapp;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.telemetry.MapboxTelemetry;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionsListener, LocationEngineListener {
  private final String LOG_TAG = "MainActivity";
  private MapboxTelemetry mapboxTelemetry;
  private PermissionsManager permissionsManager;
  private LocationEngine locationEngine;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String accessTokenTelemetry = obtainAccessToken();
    String userAgentTelemetry = "MapboxEventsAndroid/3.1.0";
    mapboxTelemetry = new MapboxTelemetry(this, accessTokenTelemetry, userAgentTelemetry);

    checkPermissions();
  }

  @Override
  protected void onStart() {
    super.onStart();
    startLocation();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    //mapboxTelemetry.disable();
  }

  private String obtainAccessToken() {
    String accessToken = getString(R.string.mapbox_access_token);
    return accessToken;
  }

  private void checkPermissions() {
    boolean permissionsGranted = PermissionsManager.areLocationPermissionsGranted(this);

    if (permissionsGranted) {
      //mapboxTelemetry.enable();
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {

  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      //mapboxTelemetry.enable();
    }
  }

  public void startLocation() {
    LocationEngineProvider locationEngineProvider = new LocationEngineProvider(getApplicationContext());

    locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
    locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
    locationEngine.addLocationEngineListener(this);

    locationEngine.activate();
  }

  @Override
  public void onConnected() {
    locationEngine.requestLocationUpdates();
  }

  @Override
  public void onLocationChanged(Location location) {
    Log.e("test", "location: " + location);
  }
}
