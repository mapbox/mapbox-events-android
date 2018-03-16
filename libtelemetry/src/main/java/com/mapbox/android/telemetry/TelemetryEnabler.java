package com.mapbox.android.telemetry;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.Map;

import static com.mapbox.android.telemetry.TelemetryUtils.obtainSharedPreferences;

public class TelemetryEnabler {
  public enum State {
    ENABLED, DISABLED
  }

  static final String MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE = "mapboxTelemetryState";
  static final Map<TelemetryEnabler.State, Boolean> TELEMETRY_STATES =
    new HashMap<TelemetryEnabler.State, Boolean>() {
      {
        put(TelemetryEnabler.State.ENABLED, true);
        put(TelemetryEnabler.State.DISABLED, false);
      }
    };
  private static final Map<String, State> STATES = new HashMap<String, State>() {
    {
      put(State.ENABLED.name(), State.ENABLED);
      put(State.DISABLED.name(), State.DISABLED);
    }
  };
  private static final String KEY_META_DATA_ENABLED = "com.mapbox.EnableEvents";
  private boolean isFromPreferences = true;
  private State currentTelemetryState = State.ENABLED;

  TelemetryEnabler(boolean isFromPreferences) {
    this.isFromPreferences = isFromPreferences;
  }

  public static State retrieveTelemetryStateFromPreferences() {
    SharedPreferences sharedPreferences = obtainSharedPreferences();
    String telemetryStateName = sharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE,
      State.ENABLED.name());

    return STATES.get(telemetryStateName);
  }

  public static State updateTelemetryState(State telemetryState) {
    SharedPreferences sharedPreferences = obtainSharedPreferences();
    SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE, telemetryState.name());
    editor.apply();

    return telemetryState;
  }

  State obtainTelemetryState() {
    if (isFromPreferences) {
      return retrieveTelemetryStateFromPreferences();
    }

    return currentTelemetryState;
  }

  State updatePreferences(State telemetryState) {
    if (isFromPreferences) {
      return updateTelemetryState(telemetryState);
    }

    currentTelemetryState = telemetryState;
    return currentTelemetryState;
  }

  static boolean isEventsEnabled(Context context) {
    try {
      ApplicationInfo appInformation = context.getPackageManager().getApplicationInfo(
        context.getPackageName(), PackageManager.GET_META_DATA);

      if (appInformation != null && appInformation.metaData != null) {
        boolean isEnabled = appInformation.metaData.getBoolean(KEY_META_DATA_ENABLED);
        return isEnabled;
      }
    } catch (PackageManager.NameNotFoundException exception) {
      exception.printStackTrace();
    }

    return true;
  }
}
