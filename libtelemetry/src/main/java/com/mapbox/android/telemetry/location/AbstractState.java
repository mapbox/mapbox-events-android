package com.mapbox.android.telemetry.location;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

abstract class AbstractState implements State {
  @LocationEngineControllerMode
  private final int type;

  AbstractState(@LocationEngineControllerMode int type) {
    this.type = type;
  }

  @CallSuper
  @NonNull
  @Override
  public State handleEvent(Event event) throws IllegalStateException {
    State nextState;
    switch (event.getType()) {
      case EventType.Background:
        nextState = new PassiveState(this, true);
        break;
      case EventType.Stopped:
        nextState = new IdleState();
        break;
      default:
        throw new IllegalStateException("Passing through " + event.getType() + " for state " + getType());
    }
    return nextState;
  }

  @LocationEngineControllerMode
  @Override
  public int getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof AbstractState)) {
      return false;
    }

    if (this == o) {
      return true;
    }
    AbstractState that = (AbstractState) o;
    return that.type == type;
  }

  @Override
  public int hashCode() {
    return type;
  }
}
