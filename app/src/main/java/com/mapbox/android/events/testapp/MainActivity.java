package com.mapbox.android.events.testapp;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.telemetry.MapboxTelemetry;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionsListener {
  private final String LOG_TAG = "MainActivity";
  private MapboxTelemetry mapboxTelemetry;
  private PermissionsManager permissionsManager;
  private LocationEngine locationEngine;
  private LocationEngineCallback<LocationEngineResult> callback = new LocationEngineCallback<LocationEngineResult>() {
    @Override
    public void onSuccess(LocationEngineResult result) {
      Log.i(LOG_TAG, "new location update: " + result.getLastLocation().toString());
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
      Log.i(LOG_TAG, exception.toString());
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String accessTokenTelemetry = obtainAccessToken();
    String userAgentTelemetry = "MapboxEventsAndroid/4.1.0";
    mapboxTelemetry = new MapboxTelemetry(this, accessTokenTelemetry, userAgentTelemetry);
    locationEngine = LocationEngineProvider.getBestLocationEngine(this);
    checkPermissions();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapboxTelemetry.enable();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapboxTelemetry.disable();
    locationEngine.removeLocationUpdates(callback);
  }

  private String obtainAccessToken() {
    String accessToken = getString(R.string.mapbox_access_token);
    return accessToken;
  }

  @SuppressLint("MissingPermission")
  private void checkPermissions() {
    boolean permissionsGranted = PermissionsManager.areLocationPermissionsGranted(this);

    if (permissionsGranted) {
      mapboxTelemetry.enable();
      locationEngine.requestLocationUpdates(getRequest(2000), callback, null);
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  private static LocationEngineRequest getRequest(long interval) {
    return new LocationEngineRequest.Builder(interval)
      .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
      .build();
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {

  }

  @SuppressLint("MissingPermission")
  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      mapboxTelemetry.enable();
      locationEngine.requestLocationUpdates(getRequest(2000), callback, null);
    }
  }
}
