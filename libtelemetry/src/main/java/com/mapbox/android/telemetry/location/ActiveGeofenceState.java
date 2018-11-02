package com.mapbox.android.telemetry.location;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.ACTIVE_GEOFENCE;

class ActiveGeofenceState extends AbstractState {
  private static final String TAG = "ActiveGeofenceState";
  private final Location lastLocation;

  ActiveGeofenceState(Location location) {
    super(ACTIVE_GEOFENCE);
    this.lastLocation = location;
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
        nextState = location.getSpeed() > 0.0f ? new ActiveGeofenceState(location) :
          new PassiveGeofenceState(location);
        break;
      case EventType.GeofenceExited:
        nextState = new ActiveState(lastLocation);
        break;
      case EventType.TimerExpired:
        nextState = new PassiveGeofenceState(lastLocation);
        break;
      default:
        throw new IllegalStateException("Unexpected event type: " + event.getType() + " for state " + getType());
    }
    return nextState;
  }
}
