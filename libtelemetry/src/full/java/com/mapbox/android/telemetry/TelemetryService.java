package com.mapbox.android.telemetry;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.mapbox.android.telemetry.LocationReceiver.LOCATION_RECEIVER_INTENT;
import static com.mapbox.android.telemetry.TelemetryReceiver.TELEMETRY_RECEIVER_INTENT;

public class TelemetryService extends Service implements TelemetryCallback, EventCallback {
  public static final String IS_LOCATION_ENABLER_FROM_PREFERENCES = "isLocationEnablerFromPreferences";
  private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
  private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
  private static final String TAG = "TelemetryService";
  private LocationReceiver locationReceiver = null;
  private TelemetryReceiver telemetryReceiver = null;
  private EventsQueue queue = null;
  private int boundInstances = 0;
  private LocationEngine locationEngine = null;
  private CopyOnWriteArraySet<ServiceTaskCallback> serviceTaskCallbacks = null;
  private TelemetryLocationEnabler telemetryLocationEnabler;
  // For testing only:
  private boolean isLocationEnablerFromPreferences = true;
  private boolean isLocationReceiverRegistered = false;
  private boolean isTelemetryReceiverRegistered = false;

  private final LocationEngineCallback<LocationEngineResult> callback =
          new LocationEngineCallback<LocationEngineResult>() {
    @Override
    public void onSuccess(LocationEngineResult result) {
      List<Location> locations = result.getLocations();
      Log.d(TAG, "Locations reported: " + locations.size());

      if (locations == null || locations.isEmpty()) {
        Log.e(TAG, "Location is unavailable");
        return;
      }

      LocalBroadcastManager.getInstance(getApplicationContext())
        .sendBroadcast(LocationReceiver.supplyIntent(locations));
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
      Log.e(TAG, exception.toString());
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    Context context = getApplicationContext();
    createLocationReceiver(context);
    createTelemetryReceiver(context);
    createServiceTaskCallbacks();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    enableTelemetryLocationState(intent, getApplicationContext());
    return START_REDELIVER_INTENT;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    // A service may return null if clients cannot bind to the service (preferred case)
    // For testing purposes a new telemetry binder is returned
    return new TelemetryBinder();
  }

  @Override
  public void onDestroy() {
    Context context = getApplicationContext();
    unregisterLocationReceiver(context);
    unregisterTelemetryReceiver(context);
    disableTelemetryLocationState(context);
    super.onDestroy();
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    for (ServiceTaskCallback callback : serviceTaskCallbacks) {
      callback.onTaskRemoved();
    }
    super.onTaskRemoved(rootIntent);
  }

  @Override
  public void onBackground() {
    // TODO Remove after including UI sample app tests
    System.out.println("TelemetryService#onBackground: Shutting down location receiver...");
    unregisterLocationReceiver(getApplicationContext());
  }

  @Override
  public void onForeground() {
    // TODO Remove after including UI sample app tests
    System.out.println("TelemetryService#onForeground: Restarting location receiver...");
    registerLocationReceiver(getApplicationContext());
  }

  @Override
  public void onEventReceived(Event event) {
    if (queue != null) {
      queue.push(event);
    }
  }

  public void updateSessionIdentifier(SessionIdentifier sessionIdentifier) {
    locationReceiver.updateSessionIdentifier(sessionIdentifier);
  }

  void bindInstance() {
    synchronized (this) {
      boundInstances++;
    }
  }

  void unbindInstance() {
    synchronized (this) {
      boundInstances--;
    }
  }

  int obtainBoundInstances() {
    synchronized (this) {
      return boundInstances;
    }
  }

  boolean addServiceTask(ServiceTaskCallback callback) {
    return serviceTaskCallbacks.add(callback);
  }

  boolean removeServiceTask(ServiceTaskCallback callback) {
    return serviceTaskCallbacks.remove(callback);
  }

  void injectEventsQueue(EventsQueue queue) {
    this.queue = queue;
  }

  @VisibleForTesting
  boolean isLocationReceiverRegistered() {
    return isLocationReceiverRegistered;
  }

  @VisibleForTesting
  boolean isTelemetryReceiverRegistered() {
    return isTelemetryReceiverRegistered;
  }

  @VisibleForTesting
  boolean locationPermissionCheck() {
    return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
      == PackageManager.PERMISSION_GRANTED;
  }

  private void createLocationReceiver(Context context) {
    locationReceiver = new LocationReceiver(this);
    registerLocationReceiver(context);
  }

  private void registerLocationReceiver(Context context) {
    // Instantiate location engine and request updates
    locationEngine = LocationEngineProvider.getBestLocationEngine(context, true);
    if (locationPermissionCheck()) {
      try {
        locationEngine.requestLocationUpdates(getRequest(), callback, getMainLooper());
      } catch (SecurityException se) {
        Log.e(TAG, se.toString());
      }
    }

    LocalBroadcastManager.getInstance(context)
      .registerReceiver(locationReceiver, new IntentFilter(LOCATION_RECEIVER_INTENT));
    isLocationReceiverRegistered = true;
  }

  private static LocationEngineRequest getRequest() {
    return new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
  }

  private void createTelemetryReceiver(Context context) {
    telemetryReceiver = new TelemetryReceiver(this);
    LocalBroadcastManager.getInstance(context)
      .registerReceiver(telemetryReceiver, new IntentFilter(TELEMETRY_RECEIVER_INTENT));
    isTelemetryReceiverRegistered = true;
  }

  private void createServiceTaskCallbacks() {
    serviceTaskCallbacks = new CopyOnWriteArraySet<>();
  }

  private void enableTelemetryLocationState(Intent intent, Context context) {
    if (intent != null) {
      isLocationEnablerFromPreferences = intent.getBooleanExtra(IS_LOCATION_ENABLER_FROM_PREFERENCES, true);
    } else {
      isLocationEnablerFromPreferences = true;
    }

    if (isLocationEnablerFromPreferences) {
      createLocationEnabler();
      telemetryLocationEnabler.updateTelemetryLocationState(TelemetryLocationEnabler.LocationState.ENABLED, context);
    }
  }

  private void unregisterLocationReceiver(Context context) {
    locationEngine.removeLocationUpdates(callback);
    LocalBroadcastManager.getInstance(context).unregisterReceiver(locationReceiver);
    isLocationReceiverRegistered = false;
  }

  private void unregisterTelemetryReceiver(Context context) {
    LocalBroadcastManager.getInstance(context).unregisterReceiver(telemetryReceiver);
    isTelemetryReceiverRegistered = false;
  }

  private void disableTelemetryLocationState(Context context) {
    if (isLocationEnablerFromPreferences) {
      createLocationEnabler();
      telemetryLocationEnabler.updateTelemetryLocationState(TelemetryLocationEnabler.LocationState.DISABLED, context);
    }
  }

  private void createLocationEnabler() {
    if (telemetryLocationEnabler == null) {
      telemetryLocationEnabler = new TelemetryLocationEnabler(true);
    }
  }

  class TelemetryBinder extends Binder {
    TelemetryService obtainService() {
      return TelemetryService.this;
    }
  }
}
