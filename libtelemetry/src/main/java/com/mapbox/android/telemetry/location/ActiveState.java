package com.mapbox.android.telemetry.location;

import android.location.Location;

import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.ACTIVE;

class ActiveState implements State {
  private static final float ACCURACY_THRESHOLD_METERS = 300f;
  private final Location lastLocation;

  ActiveState(Location location) {
    this.lastLocation = location;
  }

  @Override
  public State handleEvent(Event event) throws IllegalStateException {
    State nextState;
    switch (event.getType()) {
      case EventType.Background:
        nextState = new PassiveState(this, true);
        break;
      case EventType.TimerExpired:
        nextState = new PassiveState(this, false);
        break;
      case EventType.LocationUpdated:
        Location location = ((LocationUpdatedEvent) event).getLocation();
        nextState = location.getAccuracy() < ACCURACY_THRESHOLD_METERS ? new ActiveGeofenceState(location)
          : new ActiveState(location);
        break;
      case EventType.Stopped:
        nextState = new IdleState();
        break;
      default:
        throw new IllegalStateException("Unexpected event type: " + event.getType() + " for state " + getType());
    }
    return nextState;
  }

  @LocationEngineControllerMode
  @Override
  public int getType() {
    return ACTIVE;
  }
}
