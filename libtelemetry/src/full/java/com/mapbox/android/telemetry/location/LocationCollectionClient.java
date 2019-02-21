package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.telemetry.TelemetryUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class LocationCollectionClient implements SharedPreferences.OnSharedPreferenceChangeListener {
  public static final String MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE = "mapboxTelemetryLocationState";
  private static final Object lock = new Object();
  private static LocationCollectionClient locationCollectionClient;
  private static final String TAG = "LocCollectionClient";

  private final AtomicBoolean isEnabled = new AtomicBoolean(false);
  private final LocationEngineController locationEngineController;

  @VisibleForTesting
  LocationCollectionClient(LocationEngineController collectionController, SharedPreferences sharedPreferences) {
    this.locationEngineController = collectionController;
    sharedPreferences.registerOnSharedPreferenceChangeListener(this);
  }

  public static LocationCollectionClient install(@NonNull Context context) {
    Context applicationContext;
    if (context.getApplicationContext() == null) {
      // In shared processes content providers getApplicationContext() can return null.
      applicationContext = context;
    } else {
      applicationContext = context.getApplicationContext();
    }

    synchronized (lock) {
      if (locationCollectionClient == null) {
        locationCollectionClient = new LocationCollectionClient(new LocationEngineController(applicationContext,
          LocationEngineProvider.getBestLocationEngine(applicationContext)),
          TelemetryUtils.obtainSharedPreferences(applicationContext));
      }
    }
    return locationCollectionClient;
  }

  @NonNull
  public static LocationCollectionClient getInstance() {
    synchronized (lock) {
      if (locationCollectionClient != null) {
        return locationCollectionClient;
      } else {
        throw new IllegalStateException("LocationCollectionClient is not installed.");
      }
    }
  }

  public boolean isEnabled() {
    return isEnabled.get();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    try {
      boolean enabled = sharedPreferences.getBoolean(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE, false);
      //if (isEnabled.compareAndSet(!enabled, enabled)) {

      //}
    } catch (Exception ex) {
      // In case of a ClassCastException
      Log.e(TAG, ex.toString());
    }
  }
}
