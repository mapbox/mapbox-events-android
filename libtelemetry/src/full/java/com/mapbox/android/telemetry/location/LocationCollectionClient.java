package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.telemetry.MapboxTelemetry;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Location collector client is responsible for managing our anonymous
 * location data collection. There's only one instance of the location collector
 * that exist in the context of app process. Location collector is lifecycle aware
 * and uses location engine in a battery efficient way.
 * <p>
 * Location collector can be disabled at any point of time or uninstalled completely
 * in order to release system resources.
 */
public class LocationCollectionClient {
  public static final String LOCATION_COLLECTOR_USER_AGENT = "mapbox-android-location";
  private static final int LOCATION_COLLECTION_STATUS_UPDATED = 0;
  private static final int SESSION_ROTATION_INTERVAL_UPDATED = 1;
  private static final Object lock = new Object();
  private static LocationCollectionClient locationCollectionClient;

  @VisibleForTesting
  final LocationEngineController locationEngineController;

  private final AtomicBoolean isEnabled = new AtomicBoolean(false);
  private final HandlerThread settingsChangeHandlerThread;
  private final MapboxTelemetry telemetry;

  private Handler settingsChangeHandler;

  @VisibleForTesting
  LocationCollectionClient(LocationEngineController collectionController,
                           HandlerThread handlerThread, MapboxTelemetry telemetry) {
    this.locationEngineController = collectionController;
    this.settingsChangeHandlerThread = handlerThread;
    this.telemetry = telemetry;
    this.settingsChangeHandlerThread.start();
    this.settingsChangeHandler = new Handler(handlerThread.getLooper()) {
      @Override
      public void handleMessage(Message msg) {
        handleSettingsChangeMessage(msg);
      }
    };
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
          LocationEngineProvider.getBestLocationEngine(applicationContext), new SessionIdentifier(defaultInterval)),
          new HandlerThread("LocationSettingsChangeThread"),
          // Provide empty token as it is not available yet
          new MapboxTelemetry(applicationContext, "", LOCATION_COLLECTOR_USER_AGENT));
      }
    }
    return locationCollectionClient;
  }

  /**
   * Uninstall current location collection client.
   * @return true if uninstall was successful
   */
  public static boolean uninstall() {
    boolean uninstalled = false;
    synchronized (lock) {
      if (locationCollectionClient != null) {
        locationCollectionClient.locationEngineController.onDestroy();
        locationCollectionClient.settingsChangeHandlerThread.quit();
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
  public static LocationCollectionClient getInstance() {
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
  public void setSessionRotationInterval(long interval) {
    Message message = Message.obtain();
    message.what = SESSION_ROTATION_INTERVAL_UPDATED;
    Bundle b = new Bundle();
    b.putLong("interval", interval);
    message.setData(b);
    settingsChangeHandler.sendMessage(message);
  }

  /**
   * Returns status of location collection client.
   *
   * @return true if collection client is active, false otherwise
   */
  public boolean isEnabled() {
    return isEnabled.get();
  }

  /**
   * Change status of the location collection client.
   * <p>
   * Note: this method is not going to block your thread.
   *
   * @param enabled location collection status.
   */
  public void setEnabled(boolean enabled) {
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

  @VisibleForTesting
  Looper getSettingsLooper() {
    return settingsChangeHandlerThread.getLooper();
  }

  /**
   * Should only be used for testing!
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
        } else {
          locationEngineController.onDestroy();
        }
        break;
      case SESSION_ROTATION_INTERVAL_UPDATED:
        Bundle bundle = msg.getData();
        long interval = bundle.getLong("interval");
        locationEngineController.setSessionIdentifier(new SessionIdentifier(interval));
        break;
      default:
        break;
    }
  }
}