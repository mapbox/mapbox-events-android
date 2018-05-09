package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class GeofenceManager {
  private GeofencingClient geofencingClient;
  private ArrayList<Geofence> geofenceList;
  private PendingIntent geofencePendingIntent;
  private Context context;
  private Activity activity;

  GeofenceManager(Context context, Activity activity) {
    this.context = context;
    this.activity = activity;
    geofencingClient = LocationServices.getGeofencingClient(context);
    geofenceList = new ArrayList<>();
  }

  void addGeofence(Location location) {
    geofenceList.add(new Geofence.Builder()
      .setRequestId("currentGeofence")

      .setCircularRegion(
        location.getLatitude(),
        location.getLongitude(),
        50
      )
      .setExpirationDuration(3600000)
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
      .build()
    );
  }

  private GeofencingRequest getGeofencingRequest() {
    GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
    builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
    builder.addGeofences(geofenceList);
    return builder.build();
  }

  private PendingIntent getGeofencePendingIntent() {
    // Reuse the PendingIntent if we already have it.
    if (geofencePendingIntent != null) {
      return geofencePendingIntent;
    }
    Intent intent = new Intent(context, GeofenceIntentService.class);
    // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
    // calling addGeofences() and removeGeofences().
    geofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    return geofencePendingIntent;
  }

  @SuppressLint("MissingPermission")
  private void trackGeofence() {
    geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
      .addOnSuccessListener(activity, new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {

        }
      })
      .addOnFailureListener(activity, new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {

        }
      });
  }
}
