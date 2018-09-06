package com.mapbox.android.telemetry;


import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import static com.mapbox.android.telemetry.TelemetryUtils.obtainSharedPreferences;

class TelemetryLocationEnabler {
  enum LocationState {
    ENABLED, DISABLED
  }

  private static final String MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE = "mapboxTelemetryLocationState";
  private static final Map<String, LocationState> LOCATION_STATES = new HashMap<String, LocationState>() {
    {
      put(LocationState.ENABLED.name(), LocationState.ENABLED);
      put(LocationState.DISABLED.name(), LocationState.DISABLED);
    }
  };
  private boolean isFromPreferences = true;
  private LocationState currentTelemetryLocationState = LocationState.DISABLED;

  TelemetryLocationEnabler(boolean isFromPreferences) {
    this.isFromPreferences = isFromPreferences;
  }

  LocationState obtainTelemetryLocationState() {
    if (isFromPreferences) {
      return retrieveTelemetryLocationStateFromPreferences();
    }

    return currentTelemetryLocationState;
  }

  LocationState updateTelemetryLocationState(LocationState telemetryLocationState) {
    if (isFromPreferences) {
      return updateLocationPreferences(telemetryLocationState);
    }

    currentTelemetryLocationState = telemetryLocationState;
    return currentTelemetryLocationState;
  }

  // For testing only
  void injectTelemetryLocationState(LocationState locationState) {
    currentTelemetryLocationState = locationState;
  }

  private LocationState retrieveTelemetryLocationStateFromPreferences() {
    if (MapboxTelemetry.applicationContext == null) {
      return LOCATION_STATES.get(LocationState.DISABLED.name());
    }

    SharedPreferences sharedPreferences = obtainSharedPreferences();
    String telemetryStateName = sharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE,
      LocationState.DISABLED.name());

    return LOCATION_STATES.get(telemetryStateName);
  }

  private LocationState updateLocationPreferences(LocationState telemetryLocationState) {
    if (MapboxTelemetry.applicationContext == null) {
      return telemetryLocationState;
    }

    SharedPreferences sharedPreferences = obtainSharedPreferences();
    SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE, telemetryLocationState.name());
    editor.apply();

    return telemetryLocationState;
  }
}
