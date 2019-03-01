package com.mapbox.android.telemetry.location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineRequest;

class LocationEngineControllerImpl implements LocationEngineController {
  private static final String TAG = "LocationController";
  private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
  private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

  private final Context applicationContext;
  private final LocationEngine locationEngine;
  private SessionIdentifier sessionIdentifier;

  LocationEngineControllerImpl(@NonNull Context context,
                               @NonNull LocationEngine locationEngine,
                               @NonNull SessionIdentifier sessionIdentifier) {
    this.applicationContext = context;
    this.locationEngine = locationEngine;
    this.sessionIdentifier = sessionIdentifier;
  }

  @Override
  public void setSessionIdentifier(@NonNull SessionIdentifier sessionIdentifier) {
    this.sessionIdentifier = sessionIdentifier;
  }

  @Override
  public void onPause() {
    // noop for now
  }

  @Override
  public void onResume() {
    requestLocationUpdates();
  }

  @Override
  public void onDestroy() {
    removeLocationUpdates();
  }

  long getSessionRotationInterval() {
    return sessionIdentifier.getInterval();
  }

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
    Intent intent = new Intent(applicationContext, LocationUpdatesBroadcastReceiver.class);
    intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_LOCATION_UPDATED);
    intent.putExtra("session_id", sessionIdentifier.getSessionId());
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
