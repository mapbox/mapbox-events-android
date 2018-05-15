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

import java.util.ArrayList;
import java.util.List;

public class GeofenceIntentService extends IntentService {
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
    Log.e("Geofence Intent", "Geofence triggered");
    GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
    accessToken = intent.getStringExtra("accessToken");
    userAgent = intent.getStringExtra("userAgent");

    if (geofencingEvent.hasError()) {
      int errorCode = geofencingEvent.getErrorCode();
      Log.e("Geofence Intent", "Geofence Error: " + errorCode);
      return;
    }

    // Get the transition type.
    int geofenceTransition = geofencingEvent.getGeofenceTransition();
    Location geofenceLocation = geofencingEvent.getTriggeringLocation();

    if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
      Log.e("Geofence Intent", "Geofence exit transition");
      geofenceManager = new GeofenceManager(getApplicationContext());

      //kill old geofence
      Log.e("Geofence Intent", "Kill Old Geofence");
      List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
      List<String> toRemove = new ArrayList<>();
      for (Geofence geofence : triggeredGeofences) {
        toRemove.add(geofence.getRequestId());
      }
      geofenceManager.removeGeofence(toRemove);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        GeofenceJobService.schedule(getApplicationContext(), userAgent, accessToken);
      }
    }
  }
}
