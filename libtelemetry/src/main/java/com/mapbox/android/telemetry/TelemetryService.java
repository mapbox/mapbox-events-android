package com.mapbox.android.telemetry;


import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsManager;

import java.util.concurrent.CopyOnWriteArraySet;

import static com.mapbox.android.telemetry.LocationReceiver.LOCATION_RECEIVER_INTENT;
import static com.mapbox.android.telemetry.TelemetryReceiver.TELEMETRY_RECEIVER_INTENT;

public class TelemetryService extends Service implements TelemetryCallback, EventCallback {
  public static final String IS_LOCATION_ENABLER_FROM_PREFERENCES = "isLocationEnablerFromPreferences";
  private static final String MISSING_FINE_PERMISSION = "Detected that ACCESS_FINE_LOCATION permission is missing from"
          + " the manifest. This is a required permission for Mapbox."
          + "Please add this permission back into your manifest, "
          + "so our system can work properly";
  public static final int API_LEVEL_23 = 23;
  private static final int DEFAULT_INTERVAL_IN_MILLISECONDS = 1000;
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

  private final LocationEngineCallback<Location> callback = new LocationEngineCallback<Location>() {
    @Override
    public void onSuccess(Location result) {
      checkApplicationContext();
      LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(LocationReceiver.supplyIntent(result));
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
      Log.e(TAG, exception.toString());
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    createLocationReceiver();
    createTelemetryReceiver();
    createServiceTaskCallbacks();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    checkApplicationContext();
    enableTelemetryLocationState(intent);
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
    checkApplicationContext();
    unregisterLocationReceiver();
    unregisterTelemetryReceiver();
    disableTelemetryLocationState();
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
    unregisterLocationReceiver();
  }

  @Override
  public void onForeground() {
    // TODO Remove after including UI sample app tests
    System.out.println("TelemetryService#onForeground: Restarting location receiver...");
    registerLocationReceiver();
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

  // For testing only
  boolean isLocationReceiverRegistered() {
    return isLocationReceiverRegistered;
  }

  // For testing only
  boolean isTelemetryReceiverRegistered() {
    return isTelemetryReceiverRegistered;
  }

  private void createLocationReceiver() {
    locationReceiver = new LocationReceiver(this);
    registerLocationReceiver();
  }

  private void registerLocationReceiver() {
    // Instantiate location engine and request updates
    locationEngine = LocationEngineProvider.getBestLocationEngine(getApplicationContext(), false);
    if (locationPermissionCheck()) {
      try {
        locationEngine.requestLocationUpdates(getRequest(), callback, getMainLooper());
      } catch (SecurityException se) {
        Log.e(TAG, se.toString());
      }
    }

    LocalBroadcastManager.getInstance(getApplicationContext())
            .registerReceiver(locationReceiver, new IntentFilter(LOCATION_RECEIVER_INTENT));
    isLocationReceiverRegistered = true;
  }

  private static LocationEngineRequest getRequest() {
    return new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER).build();
  }

  private void createTelemetryReceiver() {
    telemetryReceiver = new TelemetryReceiver(this);
    registerTelemetryReceiver();
  }

  private void registerTelemetryReceiver() {
    LocalBroadcastManager.getInstance(getApplicationContext())
            .registerReceiver(telemetryReceiver, new IntentFilter(TELEMETRY_RECEIVER_INTENT));
    isTelemetryReceiverRegistered = true;
  }

  private void createServiceTaskCallbacks() {
    serviceTaskCallbacks = new CopyOnWriteArraySet<>();
  }

  private void enableTelemetryLocationState(Intent intent) {
    isLocationEnablerFromPreferences = intent.getBooleanExtra(IS_LOCATION_ENABLER_FROM_PREFERENCES, true);

    if (isLocationEnablerFromPreferences) {
      createLocationEnabler();
      telemetryLocationEnabler.updateTelemetryLocationState(TelemetryLocationEnabler.LocationState.ENABLED);
    }
  }

  private void unregisterLocationReceiver() {
    locationEngine.removeLocationUpdates(callback);

    LocalBroadcastManager.getInstance(getApplicationContext())
            .unregisterReceiver(locationReceiver);
    isLocationReceiverRegistered = false;
  }

  private void unregisterTelemetryReceiver() {
    LocalBroadcastManager.getInstance(getApplicationContext())
            .unregisterReceiver(telemetryReceiver);
    isTelemetryReceiverRegistered = false;
  }

  private void disableTelemetryLocationState() {
    if (isLocationEnablerFromPreferences) {
      createLocationEnabler();
      telemetryLocationEnabler.updateTelemetryLocationState(TelemetryLocationEnabler.LocationState.DISABLED);
    }
  }

  private void checkApplicationContext() {
    if (MapboxTelemetry.applicationContext == null) {
      MapboxTelemetry.applicationContext = getApplicationContext();
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

  private boolean locationPermissionCheck() {
    if (Build.VERSION.SDK_INT >= API_LEVEL_23) {
      return PermissionsManager.areLocationPermissionsGranted(this);
    } else {
      int finePermission = PermissionChecker.checkSelfPermission(MapboxTelemetry.applicationContext,
              Manifest.permission.ACCESS_FINE_LOCATION);
      return checkFinePermission(finePermission);
    }
  }

  private boolean checkFinePermission(int finePermission) {
    if (finePermission != PackageManager.PERMISSION_GRANTED) {
      Log.d("Missing Permission", MISSING_FINE_PERMISSION);
      return false;
    }

    return true;
  }
}
