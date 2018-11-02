package com.mapbox.android.telemetry.location;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.PASSIVE_GEOFENCE;

class PassiveGeofenceState extends BaseState {
  private static final String TAG = "PassiveGeofenceState";
  private final Location lastLocation;

  PassiveGeofenceState(Location location) {
    super(PASSIVE_GEOFENCE);
    this.lastLocation = location;
  }

  Location getLastLocation() {
    return lastLocation;
  }

  @NonNull
  @Override
  public State handleEvent(Event event) throws IllegalStateException {
    try {
      return super.handleEvent(event);
    } catch (IllegalStateException ise) {
      Log.d(TAG, ise.getMessage());
    }

    State nextState;
    switch (event.getType()) {
      case EventType.LocationUpdated:
        Location location = ((LocationUpdatedEvent) event).getLocation();
        nextState = location.getSpeed() > 0.0f ? new ActiveGeofenceState(lastLocation) :
          new PassiveGeofenceState(lastLocation);
        break;
      case EventType.GeofenceExited:
        nextState = new ActiveState(lastLocation);
        break;
      default:
        throw new IllegalStateException("Unexpected event type: " + event.getType() + " for state " + getType());
    }
    return nextState;
  }
}