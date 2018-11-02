package com.mapbox.android.telemetry.location;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.PASSIVE;

class PassiveState extends BaseState {
  private static final String TAG = "PassiveState";

  private final State previousState;
  private final boolean isBackgroundRequested;

  PassiveState(State previousState, boolean isBackgroundRequested) {
    super(PASSIVE);
    this.previousState = previousState;
    this.isBackgroundRequested = isBackgroundRequested;
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
      case EventType.Foreground:
        nextState = previousState;
        break;
      case EventType.LocationUpdated:
        Location location = ((LocationUpdatedEvent) event).getLocation();
        nextState = isBackgroundRequested ? new PassiveState(previousState, true) :
          new ActiveState(location);
        break;
      default:
        throw new IllegalStateException("Unexpected event type: " + event.getType() + " for state " + getType());
    }
    return nextState;
  }
}
