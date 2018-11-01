package com.mapbox.android.telemetry.location;

import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;

import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.geofence.GeofenceEngine;

public class LocationEngineController implements Timer.Callback {
  private static final String TAG = "TelemLocationController";
  /// TODO: move constants to config class
  private static final long DEFAULT_TIMEOUT_MILLISECONDS = 300000L;
  private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
  private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

  private final LocationEngine locationEngine;
  private final Callback callback;
  private final Timer timer;
  private GeofenceEngine geofenceEngine;

  private State currentState;
  private final LocationEngineCallback<LocationEngineResult> locationEngineCallback =
    new LocationEngineCallback<LocationEngineResult>() {
      @Override
      public void onSuccess(LocationEngineResult result) {
        Location location = result.getLastLocation();
        handleEvent(EventFactory.createLocationUpdatedEvent(location));
        callback.onLocationUpdated(location);
      }

      @Override
      public void onFailure(@NonNull Exception exception) {
        handleEvent(EventFactory.createStoppedEvent());
      }
    };

  LocationEngineController(LocationEngine locationEngine,
                           Timer timer,
                           Callback callback) {
    this.currentState = new IdleState();
    this.locationEngine = locationEngine;
    this.callback = callback;
    this.timer = timer;
  }

  public void setGeofenceEngine(GeofenceEngine geofenceEngine) {
    this.geofenceEngine = geofenceEngine;
  }

  public void onPause() {
    this.handleEvent(EventFactory.createBackgroundEvent());
  }

  public void onResume() {
    this.handleEvent(EventFactory.createForegroundEvent());
  }

  public void onDestroy() {
    this.handleEvent(EventFactory.createStoppedEvent());
  }

  @VisibleForTesting
  State getCurrentState() {
    return currentState;
  }

  @VisibleForTesting
  synchronized void handleEvent(Event event) {
    State nextState;
    try {
      nextState = currentState.handleEvent(event);
    } catch (IllegalStateException ise) {
      Log.e(TAG, ise.toString());
      return;
    }

    if (nextState == currentState) {
      return;
    }

    switch (nextState.getType()) {
      case LocationEngineControllerMode.ACTIVE:
        // 1. Request active updates
        requestActiveUpdates();
        // 2. Start timer
        timer.start(DEFAULT_TIMEOUT_MILLISECONDS);
        break;
      case LocationEngineControllerMode.ACTIVE_GEOFENCE:
        // 1. Start or update geofence
        //geofenceEngine.addGeofences();
        // 2. Start timer
        timer.start(DEFAULT_TIMEOUT_MILLISECONDS);
        break;
      case LocationEngineControllerMode.PASSIVE:
        requestPassiveUpdates();
        // cancel geofence ?
        //geofenceEngine.removeGeofences();
        timer.cancel();
        break;
      case LocationEngineControllerMode.PASSIVE_GEOFENCE:
        // This will effectively cancel active updates
        requestPassiveUpdates();
        break;
      case LocationEngineControllerMode.IDLE:
        locationEngine.removeLocationUpdates(locationEngineCallback);
        // Cancel geofence engine request
        //geofenceEngine.removeGeofences();
        timer.cancel();
        break;
      default:
        break;
    }
    currentState = nextState;
  }

  private void requestPassiveUpdates() {
    requestLocationUpdates(LocationEngineRequest.PRIORITY_NO_POWER);
  }

  private void requestActiveUpdates() {
    requestLocationUpdates(LocationEngineRequest.PRIORITY_HIGH_ACCURACY);
  }

  private void requestLocationUpdates(int priority) {
    try {
      locationEngine.getLastLocation(locationEngineCallback);
      locationEngine.requestLocationUpdates(getRequest(priority), locationEngineCallback, Looper.myLooper());
    } catch (SecurityException se) {
      se.printStackTrace();
    }
  }

  private static LocationEngineRequest getRequest(int priority) {
    return new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
      .setPriority(priority)
      .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
  }

  @Override
  public void onExpired() {
    handleEvent(EventFactory.createTimerExpiredEvent());
  }

  public interface Callback {
    void onLocationUpdated(Location location);
  }
}
