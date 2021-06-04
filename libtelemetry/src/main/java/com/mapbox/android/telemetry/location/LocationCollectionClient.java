package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.telemetry.BuildConfig;
import com.mapbox.android.telemetry.MapboxTelemetry;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.LOCATION_COLLECTOR_ENABLED;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_SHARED_PREFERENCES;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.SESSION_ROTATION_INTERVAL_MILLIS;

/**
 * Location collector client is responsible for managing our anonymous
 * location data collection. There's only one instance of the location collector
 * that exist in the context of app process. Location collector is lifecycle aware
 * and uses location engine in a battery efficient way.
 * <p>
 * Location collector can be disabled at any point of time or uninstalled completely
 * in order to release system resources.
 */
public class LocationCollectionClient implements SharedPreferences.OnSharedPreferenceChangeListener {
  public static final int DEFAULT_SESSION_ROTATION_INTERVAL_HOURS = 24;
  private static final String LOCATION_COLLECTOR_USER_AGENT = "mapbox-android-location";
  private static final String TAG = "LocationCollectionCli";
  private static final int LOCATION_COLLECTION_STATUS_UPDATED = 0;
  private static final Object lock = new Object();
  private static LocationCollectionClient locationCollectionClient;

  @VisibleForTesting
  final LocationEngineController locationEngineController;

  private final AtomicBoolean isEnabled = new AtomicBoolean(false);
  private final AtomicReference<SessionIdentifier> sessionIdentifier = new AtomicReference<>();
  private final HandlerThread settingsChangeHandlerThread;
  private final MapboxTelemetry telemetry;
  private final SharedPreferences sharedPreferences;
  private Handler settingsChangeHandler;

  @VisibleForTesting
  LocationCollectionClient(@NonNull LocationEngineController collectionController,
                           @NonNull HandlerThread handlerThread,
                           @NonNull SessionIdentifier sessionIdentifier,
                           @NonNull SharedPreferences sharedPreferences,
                           @NonNull MapboxTelemetry telemetry) {
    this.locationEngineController = collectionController;
    this.settingsChangeHandlerThread = handlerThread;
    this.sessionIdentifier.set(sessionIdentifier);
    this.telemetry = telemetry;
    this.settingsChangeHandlerThread.start();
    this.settingsChangeHandler = new Handler(handlerThread.getLooper()) {
      @Override
      public void handleMessage(Message msg) {
        try {
          handleSettingsChangeMessage(msg);
        } catch (Throwable throwable) {
          // TODO: log silent crash
          Log.e(TAG, throwable.toString());
        }
      }
    };
    this.sharedPreferences = sharedPreferences;
    initializeSharedPreferences(sharedPreferences);
  }

  /**
   * Install location collection client
   *
   * @param context         non-null reference to context object.
   * @param defaultInterval default session rotation interval.
   * @return instance of location collector client
   */
  public static LocationCollectionClient install(@NonNull Context context, long defaultInterval) {
    Context applicationContext;
    if (context.getApplicationContext() == null) {
      // In shared processes content providers getApplicationContext() can return null.
      applicationContext = context;
    } else {
      applicationContext = context.getApplicationContext();
    }

    synchronized (lock) {
      if (locationCollectionClient == null) {
        locationCollectionClient = new LocationCollectionClient(new LocationEngineControllerImpl(applicationContext,
          LocationEngineProvider.getBestLocationEngine(applicationContext), new LocationUpdatesBroadcastReceiver()),
          new HandlerThread("LocationSettingsChangeThread"),
          new SessionIdentifier(defaultInterval),
          applicationContext.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE),
          // Provide empty token as it is not available yet
          new MapboxTelemetry(applicationContext, "",
            String.format("%s/%s", LOCATION_COLLECTOR_USER_AGENT, BuildConfig.VERSION_NAME)));
      }
    }
    return locationCollectionClient;
  }

  /**
   * Uninstall current location collection client.
   *
   * @return true if uninstall was successful
   */
  static boolean uninstall() {
    boolean uninstalled = false;
    synchronized (lock) {
      if (locationCollectionClient != null) {
        locationCollectionClient.locationEngineController.onDestroy();
        locationCollectionClient.settingsChangeHandlerThread.quit();
        locationCollectionClient.sharedPreferences.unregisterOnSharedPreferenceChangeListener(locationCollectionClient);
        locationCollectionClient = null;
        uninstalled = true;
      }
    }
    return uninstalled;
  }

  /**
   * Return a valid single instance of the location client.
   * This method may throw an exception if called before install is called.
   *
   * @return instance of location client
   */
  @NonNull
  static LocationCollectionClient getInstance() {
    synchronized (lock) {
      if (locationCollectionClient != null) {
        return locationCollectionClient;
      } else {
        throw new IllegalStateException("LocationCollectionClient is not installed.");
      }
    }
  }

  /**
   * Set a session rotation interval in milliseconds.
   *
   * @param interval interval in which session id will be renewed.
   */
  void setSessionRotationInterval(long interval) {
    sessionIdentifier.set(new SessionIdentifier(interval));
  }

  /**
   * Return a session rotation interval.
   *
   * @return session rotation interval in milliseconds.
   */
  long getSessionRotationInterval() {
    return sessionIdentifier.get().getInterval();
  }

  /**
   * Return current session identifier.
   *
   * @return unique session identifier.
   */
  String getSessionId() {
    return sessionIdentifier.get().getSessionId();
  }

  /**
   * Returns status of location collection client.
   *
   * @return true if collection client is active, false otherwise
   */
  boolean isEnabled() {
    return isEnabled.get();
  }

  /**
   * Change status of the location collection client.
   * <p>
   * Note: this method is not going to block your thread.
   *
   * @param enabled location collection status.
   */
  void setEnabled(boolean enabled) {
    if (isEnabled.compareAndSet(!enabled, enabled)) {
      settingsChangeHandler.sendEmptyMessage(LOCATION_COLLECTION_STATUS_UPDATED);
    }
  }

  /**
   * Access to telemetry temporarily until it becomes singleton.
   *
   * @return instance of telemetry
   */
  MapboxTelemetry getTelemetry() {
    return telemetry;
  }

  /**
   * Should only be used for testing!
   *
   * @param mockHandler reference to mock handler
   */
  @VisibleForTesting
  void setMockHandler(Handler mockHandler) {
    this.settingsChangeHandler = mockHandler;
  }

  @VisibleForTesting
  void handleSettingsChangeMessage(Message msg) {
    switch (msg.what) {
      case LOCATION_COLLECTION_STATUS_UPDATED:
        if (isEnabled()) {
          locationEngineController.onResume();
          telemetry.enable();
        } else {
          locationEngineController.onDestroy();
          telemetry.disable();
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    try {
      if (LOCATION_COLLECTOR_ENABLED.equals(key)) {
        setEnabled(sharedPreferences.getBoolean(LOCATION_COLLECTOR_ENABLED, false));
      } else if (SESSION_ROTATION_INTERVAL_MILLIS.equals(key)) {
        setSessionRotationInterval(sharedPreferences.getLong(SESSION_ROTATION_INTERVAL_MILLIS,
          TimeUnit.HOURS.toMillis(DEFAULT_SESSION_ROTATION_INTERVAL_HOURS)));
      }
    } catch (Exception ex) {
      // In case of a ClassCastException
      Log.e(TAG, ex.toString());
    }
  }

  private void initializeSharedPreferences(SharedPreferences sharedPreferences) {
    // We ought to reset collector state at startup,
    // this wouldn't be required in future after we migrate
    // to automatic lifecycle management.
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(LOCATION_COLLECTOR_ENABLED, isEnabled.get());
    editor.putLong(SESSION_ROTATION_INTERVAL_MILLIS, sessionIdentifier.get().getInterval());
    editor.apply();
    sharedPreferences.registerOnSharedPreferenceChangeListener(this);
  }
}