package com.mapbox.android.events.testapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.mapbox.android.core.location.AndroidLocationEngine;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.telemetry.MapboxTelemetry;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionsListener, LocationEngineListener, LocationListener {
  private final String LOG_TAG = "MainActivity";
  private MapboxTelemetry mapboxTelemetry;
  private PermissionsManager permissionsManager;
  private LocationManager locationManager;
  private LocationEngine locationEngine;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String accessTokenTelemetry = obtainAccessToken();
    String userAgentTelemetry = "MapboxEventsAndroid/";
    mapboxTelemetry = new MapboxTelemetry(this, accessTokenTelemetry, userAgentTelemetry);

    checkPermissions();
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapboxTelemetry.disable();
  }

  private String obtainAccessToken() {
    String accessToken = getString(R.string.mapbox_access_token);
    return accessToken;
  }

  private void checkPermissions() {
    boolean permissionsGranted = PermissionsManager.areLocationPermissionsGranted(this);

    if (permissionsGranted) {
      mapboxTelemetry.enable();
      startLocationTracking();
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
      mapboxTelemetry.enable();
      startLocationTracking();
    }
  }

  @SuppressLint("MissingPermission")
  private void startLocationTracking() {

    locationEngine = new AndroidLocationEngine(this);
    locationEngine.addLocationEngineListener(this);
    locationEngine.activate();
    locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
    locationEngine.requestLocationUpdates();

    startDisableTimer();
  }

  private void startDisableTimer() {
    new CountDownTimer(30000, 1000) {

      public void onTick(long millisUntilFinished) {

      }

      public void onFinish() {
        Log.v("dropped-location", "disable fired");
        mapboxTelemetry.disable();
      }
    }.start();
  }

  @Override
  public void onConnected() {
    Log.e("dropped-location", "connected");
  }

  @Override
  public void onLocationChanged(Location location) {
    Log.v("dropped-location", "location: " + location);
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    Log.e("dropped-location", "location status changed: " + provider + ", " + status);
  }

  @Override
  public void onProviderEnabled(String provider) {
    Log.e("dropped-location", "provider enabled: " + provider);
  }

  @Override
  public void onProviderDisabled(String provider) {
    Log.e("dropped-location", "provider disabled: " + provider);
  }
}
