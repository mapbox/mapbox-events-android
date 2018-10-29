package com.mapbox.android.telemetry.location;

import android.location.Location;

import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.PASSIVE;

class PassiveState implements State {
  private final State previousState;
  private final boolean isBackgroundRequested;

  PassiveState(State previousState, boolean isBackgroundRequested) {
    this.previousState = previousState;
    this.isBackgroundRequested = isBackgroundRequested;
  }

  @Override
  public State handleEvent(Event event) throws IllegalStateException {
    State nextState = null;
    switch (event.getType()) {
      case EventType.Foreground:
        nextState = previousState;
        break;
      case EventType.Background:
        nextState = new PassiveState(this, true);
        break;
      case EventType.LocationUpdated:
        Location location = ((LocationUpdatedEvent) event).getLocation();
        nextState = isBackgroundRequested ? new PassiveState(previousState, true) :
          new ActiveState(location);
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
    return PASSIVE;
  }
}
