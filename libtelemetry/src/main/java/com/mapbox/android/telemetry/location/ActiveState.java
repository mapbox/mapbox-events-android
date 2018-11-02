package com.mapbox.android.telemetry.location;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.ACTIVE;

class ActiveState extends BaseState {
  private static final String TAG = "ActiveState";
  private static final float ACCURACY_THRESHOLD_METERS = 300f;
  private final Location lastLocation;

  ActiveState(Location location) {
    super(ACTIVE);
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
      case EventType.TimerExpired:
        nextState = new PassiveState(this, false);
        break;
      case EventType.LocationUpdated:
        Location location = ((LocationUpdatedEvent) event).getLocation();
        nextState = location.getAccuracy() < ACCURACY_THRESHOLD_METERS ? new ActiveGeofenceState(location)
          : new ActiveState(location);
        break;
      default:
        throw new IllegalStateException("Unexpected event type: " + event.getType() + " for state " + getType());
    }
    return nextState;
  }
}
