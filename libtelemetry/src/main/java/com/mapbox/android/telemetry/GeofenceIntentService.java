package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceIntentService extends IntentService {
  private final String LOG_TAG = "GeofenceIntentService";
  private String accessToken;
  private String userAgent;
  private GeofenceManager geofenceManager;

  public GeofenceIntentService() {
    super("GeofenceService");
  }

  //dwell = 4
  //enter = 1
  //exit = 2
  @SuppressLint("MissingPermission")
  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    Log.e(LOG_TAG, "Geofence triggered");
    GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
    accessToken = intent.getStringExtra("accessToken");
    userAgent = intent.getStringExtra("userAgent");

    Log.e(LOG_TAG, "userAgent3: " + userAgent);

    if (geofencingEvent.hasError()) {
      int errorCode = geofencingEvent.getErrorCode();
      Log.e(LOG_TAG, "Geofence Error: " + errorCode);
      return;
    }

    // Get the transition type.
    int geofenceTransition = geofencingEvent.getGeofenceTransition();
    Location geofenceLocation = geofencingEvent.getTriggeringLocation();

    Log.e(LOG_TAG, "Geofence transition: " + geofenceTransition);
    Log.e(LOG_TAG, "Geofence location: " + geofenceLocation);

    if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
      Log.e(LOG_TAG, "Geofence exit transition");
      geofenceManager = new GeofenceManager(getApplicationContext());

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Log.e(LOG_TAG, "userAgent4: " + userAgent);
        GeofenceJobService.schedule(getApplicationContext(), userAgent, accessToken);
      }
    }
  }
}
