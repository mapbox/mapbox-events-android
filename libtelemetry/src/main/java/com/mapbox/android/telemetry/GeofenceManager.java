package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeofenceManager {
  private final String LOG_TAG = "GeofenceManager";
  private final int TWELVE_HOURS = 43200000;
  private GeofencingClient geofencingClient;
  private ArrayList<Geofence> geofenceList;
  private PendingIntent geofencePendingIntent;
  private Context context;
  private String userAgent;
  private String accessToken;

  GeofenceManager(Context context) {
    this.context = context;
    geofencingClient = LocationServices.getGeofencingClient(context);
    geofenceList = new ArrayList<>();
  }

  void addGeofence(Location location) {
    //remove existing geofence
    List<String> toRemove = new ArrayList<>();
    toRemove.add("currentGeofence");
    removeGeofence(toRemove);

    geofenceList.add(new Geofence.Builder()
      .setRequestId("currentGeofence")

      .setCircularRegion(
        location.getLatitude(),
        location.getLongitude(),
        100
      )
      .setExpirationDuration(TWELVE_HOURS)
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
      .setLoiteringDelay(150000)
      .build()
    );

    trackGeofence();
  }

  void setTelemParameters(String accessToken, String userAgent) {
    Log.e(LOG_TAG, "userAgent1: " + userAgent);
    this.accessToken = accessToken;
    this.userAgent = userAgent;
  }

  private GeofencingRequest getGeofencingRequest() {
    GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
    builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
    builder.addGeofences(geofenceList);
    return builder.build();
  }

  private PendingIntent getGeofencePendingIntent() {
    if (geofencePendingIntent != null) {
      return geofencePendingIntent;
    }

    Log.e(LOG_TAG, "userAgent2: " + userAgent);
    Intent intent = new Intent(context, GeofenceIntentService.class);
    intent.putExtra("userAgent", userAgent);
    intent.putExtra("accessToken", accessToken);

    geofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    return geofencePendingIntent;
  }

  @SuppressLint("MissingPermission")
  private void trackGeofence() {
    geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent());
  }

  void removeGeofence(List<String> geofenceList) {
    geofencingClient.removeGeofences(geofenceList);
  }

  void stopGeofenceMonitoring() {
    geofencingClient.removeGeofences(getGeofencePendingIntent());
  }
}
