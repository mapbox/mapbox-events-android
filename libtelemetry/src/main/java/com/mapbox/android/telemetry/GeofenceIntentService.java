package com.mapbox.android.telemetry;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceIntentService extends IntentService{
  /**
   * Creates an IntentService.  Invoked by your subclass's constructor.
   */
  public GeofenceIntentService() {
    super("GeofenceService");
  }

  //dwell = 4
  //enter = 1
  //exit = 2

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

    if (geofencingEvent.hasError()) {
      int errorCode = geofencingEvent.getErrorCode();
      Log.e("Geofence Intent", "Geofence Error: " + errorCode);
      return;
    }

    // Get the transition type.
    int geofenceTransition = geofencingEvent.getGeofenceTransition();
    Location geofenceLocation = geofencingEvent.getTriggeringLocation();
    List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

    if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

    }

    Log.e("Geofence Intent", "transitionType: " + geofenceTransition);
    Log.e("Geofence Intent", "trigger location: " + geofenceLocation);
    Log.e("Geofence Intent", "triggeredGeofences: " + triggeredGeofences);


  }
}
