package com.mapbox.android.telemetry;


import android.content.Context;
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
  private boolean isFromPreferences;
  private LocationState currentTelemetryLocationState = LocationState.DISABLED;

  TelemetryLocationEnabler(boolean isFromPreferences) {
    this.isFromPreferences = isFromPreferences;
  }

  LocationState obtainTelemetryLocationState(Context context) {
    if (isFromPreferences) {
      return retrieveTelemetryLocationStateFromPreferences(context);
    }
    return currentTelemetryLocationState;
  }

  LocationState updateTelemetryLocationState(LocationState telemetryLocationState, Context context) {
    if (isFromPreferences) {
      return updateLocationPreferences(telemetryLocationState, context);
    }

    currentTelemetryLocationState = telemetryLocationState;
    return currentTelemetryLocationState;
  }

  // For testing only
  void injectTelemetryLocationState(LocationState locationState) {
    currentTelemetryLocationState = locationState;
  }

  private LocationState retrieveTelemetryLocationStateFromPreferences(Context context) {
    SharedPreferences sharedPreferences = obtainSharedPreferences(context);
    String telemetryStateName = sharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE,
      LocationState.DISABLED.name());

    return telemetryStateName != null ? LOCATION_STATES.get(telemetryStateName) :
      LOCATION_STATES.get(LocationState.DISABLED.name());
  }

  private LocationState updateLocationPreferences(LocationState telemetryLocationState, Context context) {
    SharedPreferences sharedPreferences = obtainSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE, telemetryLocationState.name());
    editor.apply();
    return telemetryLocationState;
  }
}
