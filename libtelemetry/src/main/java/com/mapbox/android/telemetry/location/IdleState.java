package com.mapbox.android.telemetry.location;

import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.IDLE;

class IdleState implements State {
  @Override
  public State handleEvent(Event event) throws IllegalStateException {
    State nextState;
    switch (event.getType()) {
      case EventType.Foreground:
        nextState = new ActiveState(null);
        break;
      case EventType.Background:
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
    return IDLE;
  }
}
