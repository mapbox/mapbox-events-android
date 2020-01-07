package com.mapbox.android.telemetry.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.util.Log;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineRequest;

class LocationEngineControllerImpl implements LocationEngineController {
  private static final String TAG = "LocationController";
  private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
  private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

  private final Context applicationContext;
  private final LocationEngine locationEngine;
  private final LocationUpdatesBroadcastReceiver locationUpdatesBroadcastReceiver;

  LocationEngineControllerImpl(@NonNull Context context,
                               @NonNull LocationEngine locationEngine,
                               @NonNull LocationUpdatesBroadcastReceiver locationUpdatesBroadcastReceiver) {
    this.applicationContext = context;
    this.locationEngine = locationEngine;
    this.locationUpdatesBroadcastReceiver = locationUpdatesBroadcastReceiver;
  }

  @Override
  public void onPause() {
    // noop for now
  }

  @Override
  public void onResume() {
    registerReceiver();
    requestLocationUpdates();
  }

  @Override
  public void onDestroy() {
    removeLocationUpdates();
    unregisterReceiver();
  }

  private void registerReceiver() {
    try {
      applicationContext.registerReceiver(locationUpdatesBroadcastReceiver,
        new IntentFilter(LocationUpdatesBroadcastReceiver.ACTION_LOCATION_UPDATED));
    } catch (IllegalArgumentException iae) {
      Log.e(TAG, iae.toString());
    }
  }

  private void unregisterReceiver() {
    try {
      applicationContext.unregisterReceiver(locationUpdatesBroadcastReceiver);
    } catch (IllegalArgumentException iae) {
      Log.e(TAG, iae.toString());
    }
  }

  @SuppressLint("MissingPermission")
  private void requestLocationUpdates() {
    if (!checkPermissions()) {
      Log.w(TAG, "Location permissions are not granted");
      return;
    }

    try {
      locationEngine.requestLocationUpdates(createRequest(DEFAULT_INTERVAL_IN_MILLISECONDS), getPendingIntent());
    } catch (SecurityException se) {
      Log.e(TAG, se.toString());
    }
  }

  private void removeLocationUpdates() {
    locationEngine.removeLocationUpdates(getPendingIntent());
  }

  private PendingIntent getPendingIntent() {
    // Implicit intent is required here to work with registering receiver via context
    Intent intent = new Intent(LocationUpdatesBroadcastReceiver.ACTION_LOCATION_UPDATED);
    return PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private boolean checkPermissions() {
    return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
      == PackageManager.PERMISSION_GRANTED;
  }

  private static LocationEngineRequest createRequest(long interval) {
    return new LocationEngineRequest.Builder(interval)
      .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
      .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
  }
}
