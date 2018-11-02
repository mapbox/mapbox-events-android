package com.mapbox.android.telemetry.location;

import android.support.annotation.NonNull;
import android.util.Log;

import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.IDLE;

class IdleState extends BaseState {
  private static final String TAG = "IdleState";

  IdleState() {
    super(IDLE);
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
        nextState = new ActiveState(null);
        break;
      default:
        throw new IllegalStateException("Unexpected event type: " + event.getType() + " for state " + getType());
    }
    return nextState;
  }
}
