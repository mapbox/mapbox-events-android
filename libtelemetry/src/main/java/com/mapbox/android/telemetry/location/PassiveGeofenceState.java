package com.mapbox.android.telemetry.location;

import android.location.Location;

import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.PASSIVE_GEOFENCE;

class PassiveGeofenceState implements State {
  private final Location lastLocation;

  PassiveGeofenceState(Location location) {
    this.lastLocation = location;
  }

  @Override
  public State handleEvent(Event event) throws IllegalStateException {
    State nextState;
    switch (event.getType()) {
      case EventType.Background:
        nextState = new PassiveState(this, true);
        break;
      case EventType.LocationUpdated:
        Location location = ((LocationUpdatedEvent) event).getLocation();
        nextState = location.getSpeed() > 0.0f ? new ActiveGeofenceState(lastLocation) :
          new PassiveGeofenceState(lastLocation);
        break;
      case EventType.GeofenceExited:
        nextState = new ActiveState(lastLocation);
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
    return PASSIVE_GEOFENCE;
  }
}
