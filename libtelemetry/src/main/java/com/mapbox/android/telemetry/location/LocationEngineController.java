package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.google.android.gms.location.GeofencingRequest;
import com.mapbox.android.core.api.BroadcastReceiverProxy;
import com.mapbox.android.core.api.IntentHandler;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;

public class LocationEngineController {
  private static final String TAG = "TelemLocationController";
  /// TODO: move constants to config class
  private static final int MAX_EVENTS_IN_QUEUE = 10;
  private static final long DEFAULT_TIMEOUT_MILLISECONDS = 20000L; //300000L;
  private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
  private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
  private static final float DEFAULT_GEOFENCE_RADIUS_METERS = 300.0f;

  private final LocationEngine locationEngine;
  private final Callback callback;
  private final Timer timer;
  private final EventDispatcher eventDispatcher;

  @Nullable
  private GeofenceEngine geofenceEngine;

  private State currentState;
  private final LocationEngineCallback<LocationEngineResult> locationEngineCallback =
    new LocationEngineCallback<LocationEngineResult>() {
      @Override
      public void onSuccess(LocationEngineResult result) {
        Location location = result.getLastLocation();
        eventDispatcher.enqueue(EventFactory.createLocationUpdatedEvent(location));
        callback.onLocationUpdated(location);
      }

      @Override
      public void onFailure(@NonNull Exception exception) {
        eventDispatcher.enqueue(EventFactory.createStoppedEvent());
      }
    };

  LocationEngineController(LocationEngine locationEngine,
                           EventDispatcher eventDispatcher,
                           Timer timer,
                           Callback callback) {
    this.currentState = new IdleState();
    this.locationEngine = locationEngine;
    this.eventDispatcher = eventDispatcher;
    eventDispatcher.setLocationEngineController(this);
    this.callback = callback;

    this.timer = timer;
    this.timer.setCallback(getTimerCallback(this.eventDispatcher));
  }

  public static LocationEngineController create(Context context,
                                                LocationEngine locationEngine,
                                                Looper looper,
                                                LocationEngineController.Callback callback) {
    return new LocationEngineController(locationEngine,
      EventDispatcher.create(MAX_EVENTS_IN_QUEUE), new LocationUpdateTrigger(looper), callback)
      .setGeofenceEngine(GoogleGeofenceEngine.create(context), new GeofenceEventBroadcastReceiverProxy(context));
  }

  @VisibleForTesting
  LocationEngineController setGeofenceEngine(GeofenceEngine geofenceEngine, BroadcastReceiverProxy proxy) {
    if (geofenceEngine == null) {
      return this;
    }
    geofenceEngine.setBroadCastReceiverProxy(proxy, getIntentHandler(this.eventDispatcher));
    this.geofenceEngine = geofenceEngine;
    return this;
  }

  public void onPause() {
    eventDispatcher.enqueue(EventFactory.createBackgroundEvent());
  }

  public void onResume() {
    eventDispatcher.enqueue(EventFactory.createForegroundEvent());
  }

  public void onDestroy() {
    eventDispatcher.enqueue(EventFactory.createStoppedEvent());
  }

  @VisibleForTesting
  State getCurrentState() {
    return currentState;
  }

  synchronized void handleEvent(Event event) {
    State nextState;
    try {
      nextState = currentState.handleEvent(event);
    } catch (IllegalStateException ise) {
      Log.e(TAG, ise.toString());
      return;
    }

    if (!nextState.equals(currentState)) {
      switch (nextState.getType()) {
        case LocationEngineControllerMode.ACTIVE:
          // 1. Request active updates
          requestActiveUpdates();
          // 2. Start timer
          timer.start(DEFAULT_TIMEOUT_MILLISECONDS);
          break;
        case LocationEngineControllerMode.ACTIVE_GEOFENCE:
          // 1. Start or update geofence
          Location location = ((ActiveGeofenceState) nextState).getLastLocation();
          createGeofenceAround(location);
          // 2. Start timer
          timer.start(DEFAULT_TIMEOUT_MILLISECONDS);
          break;
        case LocationEngineControllerMode.PASSIVE:
          requestPassiveUpdates();
          removeGeofence();
          timer.cancel();
          break;
        case LocationEngineControllerMode.PASSIVE_GEOFENCE:
          // This will effectively cancel active updates
          requestPassiveUpdates();
          timer.cancel();
          break;
        case LocationEngineControllerMode.IDLE:
          locationEngine.removeLocationUpdates(locationEngineCallback);
          // Cancel geofence engine request
          removeGeofence();
          timer.cancel();
          break;
        default:
          break;
      }
    }
    currentState = nextState;
  }

  private void createGeofenceAround(Location location) {
    if (geofenceEngine == null) {
      Log.w(TAG, "Geofencing is not supported");
      return;
    }
    GeofencingRequest request = geofenceEngine.getGeofencingRequest(location, DEFAULT_GEOFENCE_RADIUS_METERS);
    geofenceEngine.addGeofences(request);
    geofenceEngine.subscribe();
  }

  private void removeGeofence() {
    if (geofenceEngine == null) {
      Log.w(TAG, "Geofencing is not supported");
      return;
    }
    geofenceEngine.removeGeofences();
    geofenceEngine.unsubscribe();
  }

  private void requestPassiveUpdates() {
    requestLocationUpdates(LocationEngineRequest.PRIORITY_NO_POWER);
  }

  private void requestActiveUpdates() {
    requestLocationUpdates(LocationEngineRequest.PRIORITY_HIGH_ACCURACY);
  }

  private void requestLocationUpdates(int priority) {
    try {
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

  private Timer.Callback getTimerCallback(final EventDispatcher eventDispatcher) {
    return new Timer.Callback() {
      @Override
      public void onExpired() {
        eventDispatcher.enqueue(EventFactory.createTimerExpiredEvent());
      }
    };
  }

  private IntentHandler getIntentHandler(final EventDispatcher eventDispatcher) {
    return new IntentHandler() {
      @Override
      public void handle(Intent intent) {
        eventDispatcher.enqueue(EventFactory.createGeofenceExiteEvent());
      }
    };
  }

  public interface Callback {
    void onLocationUpdated(Location location);
  }
}
