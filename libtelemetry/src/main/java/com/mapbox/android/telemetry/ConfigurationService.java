package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.android.core.crashreporter.ErrorReporter;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_CONFIGURATION;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_TELEMETRY_PACKAGE;
import static com.mapbox.android.telemetry.TelemetryEnabler.MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE;
import static com.mapbox.android.telemetry.TelemetryEnabler.State;

public class ConfigurationService implements SharedPreferences.OnSharedPreferenceChangeListener {
  private static final String LOG_TAG = "ConfigurationService";
  private static final String CONFIG_ERROR_MESSAGE = "Unexpected configuration %s";

  private Context context;
  private ConfigurationCallback callback;
  private State currentState;

  public ConfigurationService(@NonNull Context context, @NonNull ConfigurationCallback callback) {
    this.context = context.getApplicationContext();
    this.callback = callback;
    init();
  }

  private void init() {
    this.currentState = new TelemetryEnabler(true).obtainTelemetryState();
    TelemetryUtils.obtainSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
  }

  public void onDestroy() {
    TelemetryUtils.obtainSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);
  }

  @VisibleForTesting
  @RestrictTo(RestrictTo.Scope.TESTS)
  void setCurrentState(State currentState) {
    this.currentState = currentState;
  }

  public void updateConfiguration(Configuration configuration) {
    Gson gson = new GsonBuilder().serializeNulls().create();
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(MAPBOX_CONFIGURATION, gson.toJson(configuration));
    editor.apply();

    updateTelemetryState(configuration);
  }

  @Nullable
  public Configuration getConfiguration() {
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    String configuration = sharedPreferences.getString(MAPBOX_CONFIGURATION, null);

    return configuration != null ? new Gson().fromJson(configuration, Configuration.class) : null;
  }

  @VisibleForTesting
  boolean updateTelemetryState(Configuration configuration) {
    boolean updated = false;
    State updatedState = getUpdatedTelemetryState(currentState, configuration);

    if (shouldUpdateTelemetryState(currentState, updatedState)) {
      TelemetryEnabler.updateTelemetryState(updatedState);
      if (currentState == State.CONFIG_DISABLED && (updatedState == State.ENABLED || updatedState == State.OVERRIDE)) {
        callback.configurationChanged(true);
        updated = true;
      } else if ((currentState == State.ENABLED || currentState == State.OVERRIDE)
        && updatedState == State.CONFIG_DISABLED) {
        callback.configurationChanged(false);
        updated = true;
      } else {
        Log.d(LOG_TAG, "updateTelemetryState");
      }

      currentState = updatedState;
    }

    return updated;
  }

  @VisibleForTesting
  void updateTelemetryState() {
    State updatedState = new TelemetryEnabler(true).obtainTelemetryState();
    boolean sendStateChangeMessage = currentState != updatedState;
    switch (updatedState) {
      case ENABLED:
        if (sendStateChangeMessage) {
          callback.configurationChanged(true);
        }
        break;
      case DISABLED:
        if (sendStateChangeMessage) {
          callback.configurationChanged(false);
        }
        break;
      default:
        Log.d(LOG_TAG, String.format("Unable to react to state %s", updatedState.name()));
    }
  }

  @VisibleForTesting
  boolean shouldUpdateTelemetryState(State currentState, State updatedState) {
    return updatedState != currentState && currentState != State.DISABLED;
  }

  @VisibleForTesting
  State getUpdatedTelemetryState(State currentState, Configuration configuration) {
    State updatedState = currentState;
    Integer type = configuration.getType();
    if (type != null) {
      switch (type) {
        case 0:
          updatedState = State.OVERRIDE;
          break;
        case 1:
          updatedState = State.CONFIG_DISABLED;
          break;
        default:
          reportError(String.format(CONFIG_ERROR_MESSAGE, configuration.toString()));
      }
    } else {
      updatedState = State.ENABLED;
    }

    return updatedState;
  }

  void reportError(final String message) {
    ErrorReporter.reportError(context, MAPBOX_TELEMETRY_PACKAGE, new Throwable(message));
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    try {
      if (MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE.equals(key)) {
        updateTelemetryState();
      }
    } catch (Exception exception) {
      Log.e(LOG_TAG, exception.toString());
    }
  }
}
