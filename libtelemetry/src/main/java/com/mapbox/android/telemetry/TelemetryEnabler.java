package com.mapbox.android.telemetry;


import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import static com.mapbox.android.telemetry.TelemetryUtils.obtainSharedPreferences;

public class TelemetryEnabler {
  public enum State {
    NOT_INITIALIZED, ENABLED, DISABLED
  }

  static final String MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE = "mapboxTelemetryState";
  static final Map<TelemetryEnabler.State, Boolean> TELEMETRY_STATES =
    new HashMap<TelemetryEnabler.State, Boolean>() {
      {
        put(TelemetryEnabler.State.NOT_INITIALIZED, false);
        put(TelemetryEnabler.State.ENABLED, true);
        put(TelemetryEnabler.State.DISABLED, false);
      }
    };
  private static final Map<String, State> STATES = new HashMap<String, State>() {
    {
      put(State.NOT_INITIALIZED.name(), State.NOT_INITIALIZED);
      put(State.ENABLED.name(), State.ENABLED);
      put(State.DISABLED.name(), State.DISABLED);
    }
  };
  private boolean isFromPreferences = true;
  private State currentTelemetryState = State.NOT_INITIALIZED;

  TelemetryEnabler(boolean isFromPreferences) {
    this.isFromPreferences = isFromPreferences;
  }

  public static State retrieveTelemetryStateFromPreferences() {
    SharedPreferences sharedPreferences = obtainSharedPreferences();
    String telemetryStateName = sharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE,
      State.NOT_INITIALIZED.name());

    return STATES.get(telemetryStateName);
  }

  State obtainTelemetryState() {
    if (isFromPreferences) {
      return retrieveTelemetryStateFromPreferences();
    }

    return currentTelemetryState;
  }

  State updateTelemetryState(State telemetryState) {
    if (isFromPreferences) {
      return updatePreferences(telemetryState);
    }

    currentTelemetryState = telemetryState;
    return currentTelemetryState;
  }

  void injectTelemetryState(State state) {
    currentTelemetryState = state;
  }

  private State updatePreferences(State telemetryState) {
    SharedPreferences sharedPreferences = obtainSharedPreferences();
    SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE, telemetryState.name());
    editor.apply();

    return telemetryState;
  }
}
