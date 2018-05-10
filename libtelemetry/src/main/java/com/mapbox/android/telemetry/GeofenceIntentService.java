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
   *
   * @param name Used to name the worker thread, important only for debugging.
   */
  public GeofenceIntentService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    Log.e("Geofence Intent", "onHandleIntent");
    GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

    // Get the transition type.
    int geofenceTransition = geofencingEvent.getGeofenceTransition();
    int errorCode = geofencingEvent.getErrorCode();
    Location geofenceLocation = geofencingEvent.getTriggeringLocation();
    List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

    Log.e("Geofence Intent", "transitionType: " + geofenceTransition);
//    Log.e("Geofence Intent", "errorCode: " + errorCode);
    Log.e("Geofence Intent", "trigger location: " + geofenceLocation);
    Log.e("Geofence Intent", "triggeredGeofences: " + triggeredGeofences);

//    // Test that the reported transition was of interest.
//    if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
//      geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
//
//      // Get the geofences that were triggered. A single event can trigger
//      // multiple geofences.
//      List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
//
//      Log.e("Geofence Intent", geofencingEvent.toString());
//
//      // Get the transition details as a String.
//      String geofenceTransitionDetails = getGeofenceTransitionDetails(
//        this,
//        geofenceTransition,
//        triggeringGeofences
//      );
//
//      // Send notification and log the transition details.
//      sendNotification(geofenceTransitionDetails);
//      Log.e("Geofence Intent", geofenceTransitionDetails);
//    } else {
//      // Log the error.
//      Log.e("Geofence Intent", getString(R.string.geofence_transition_invalid_type, geofenceTransition));
//    }
  }
}
