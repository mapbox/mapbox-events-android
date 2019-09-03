package com.mapbox.android.telemetry;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import static com.mapbox.android.telemetry.TelemetryUtils.obtainSharedPreferences;

/**
 * Do not use this class outside of activity!!!
 */
public class TelemetryEnabler {
  public enum State {
    ENABLED, DISABLED
  }


  // It is intentional to keep this a Class Level variable, so as to keep its life tied to the instance.
  // Since these listeners are stored as WeakReference's, they may get garbage collected
  // over time if declared as a local variable.
  // https://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently
  @SuppressWarnings("FieldCanBeLocal")
  private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

  public static final String MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE = "mapboxTelemetryState";
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
  private boolean isFromPreferences;
  private State currentTelemetryState;

  private Context context;

  TelemetryEnabler(boolean isFromPreferences, Context applicationContext) {

    this.isFromPreferences = isFromPreferences;

    if (isFromPreferences) {

      if (applicationContext == null) {
        throw new IllegalStateException(" Context not provided. Can not fetch Telemetry State");
      }

      this.context = applicationContext;

      SharedPreferences sharedPreferences = obtainSharedPreferences(context);
      String telemetryStateName = sharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE,
        State.ENABLED.name());

      currentTelemetryState = STATES.get(telemetryStateName);

      prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

          if (s.equals(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE)) {

            String telemetryStateName = sharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE,
              State.ENABLED.name());

            currentTelemetryState = STATES.get(telemetryStateName);
          }
        }
      };

      sharedPreferences.registerOnSharedPreferenceChangeListener(prefListener);

    } else {

      currentTelemetryState = State.ENABLED;
    }
  }

  private State retrieveTelemetryStateFromPreferences() {

    return currentTelemetryState;
  }

  synchronized State updateTelemetryState(State telemetryState) {

    if (isFromPreferences) {

      if (context == null) {
        return telemetryState;
      }

      updatePreferences(telemetryState);

    } else {

      currentTelemetryState = telemetryState;
    }
    return currentTelemetryState;

  }

  @NonNull
  State obtainTelemetryState() {
    if (isFromPreferences) {
      return retrieveTelemetryStateFromPreferences();
    }

    return currentTelemetryState;
  }

  private void updatePreferences(State telemetryState) {

    SharedPreferences sharedPreferences = obtainSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE, telemetryState.name());
    editor.apply();

  }

  static boolean isEventsEnabled(Context context) {
    try {
      ApplicationInfo appInformation = context.getPackageManager().getApplicationInfo(
        context.getPackageName(), PackageManager.GET_META_DATA);

      if (appInformation != null && appInformation.metaData != null) {
        boolean isEnabled = appInformation.metaData.getBoolean(KEY_META_DATA_ENABLED, true);
        return isEnabled;
      }
    } catch (PackageManager.NameNotFoundException exception) {
      exception.printStackTrace();
    }

    return true;
  }
}
