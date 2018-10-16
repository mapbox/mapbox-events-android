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
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsManager;

import java.util.concurrent.CopyOnWriteArraySet;

import static com.mapbox.android.telemetry.LocationReceiver.LOCATION_RECEIVER_INTENT;
import static com.mapbox.android.telemetry.TelemetryReceiver.TELEMETRY_RECEIVER_INTENT;

public class TelemetryService extends Service implements TelemetryCallback, LocationEngineListener, EventCallback {
  public static final String IS_LOCATION_ENABLER_FROM_PREFERENCES = "isLocationEnablerFromPreferences";
  private static final String MISSING_FINE_PERMISSION = "Detected that ACCESS_FINE_LOCATION permission is missing from"
          + " the manifest. This is a required permission for Mapbox."
          + "Please add this permission back into your manifest, "
          + "so our system can work properly";
  private static final String NULL_APPLICATION_CONTEXT = "MapboxTelemetry.applicationContext is null. Preventing call "
    + "of methods that require a non-null context.";
  public static final int API_LEVEL_23 = 23;
  private LocationReceiver locationReceiver = null;
  private TelemetryReceiver telemetryReceiver = null;
  private EventsQueue queue = null;
  private int boundInstances = 0;
  private LocationEngine locationEngine = null;
  private LocationEnginePriority locationPriority = LocationEnginePriority.NO_POWER;
  private CopyOnWriteArraySet<ServiceTaskCallback> serviceTaskCallbacks = null;
  private TelemetryLocationEnabler telemetryLocationEnabler;
  // For testing only:
  private boolean isLocationEnablerFromPreferences = true;
  private boolean isLocationReceiverRegistered = false;
  private boolean isTelemetryReceiverRegistered = false;

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
  @SuppressWarnings( {"MissingPermission"})
  public void onConnected() {
    locationEngine.requestLocationUpdates();
  }

  @Override
  public void onLocationChanged(Location location) {
    checkApplicationContext();
    LocalBroadcastManager.getInstance(this).sendBroadcast(LocationReceiver.supplyIntent(location));
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

  public void updateLocationPriority(LocationEnginePriority priority) {
    locationPriority = priority;

    if (locationEngine != null) {
      disconnectLocationEngine();
      setupLocationEngine();
      activateLocationEngine();
    }
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
    connectLocationEngine();
    LocalBroadcastManager.getInstance(getApplicationContext())
      .registerReceiver(locationReceiver, new IntentFilter(LOCATION_RECEIVER_INTENT));
    isLocationReceiverRegistered = true;
  }

  private void connectLocationEngine() {
    obtainLocationEngine();
    setupLocationEngine();

    if (locationPermissionCheck()) {
      activateLocationEngine();
    }
  }

  private void obtainLocationEngine() {
    locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
  }

  private void setupLocationEngine() {
    locationEngine.setPriority(locationPriority);
    locationEngine.addLocationEngineListener(this);
  }

  private void activateLocationEngine() {
    locationEngine.activate();
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
    disconnectLocationEngine();
    LocalBroadcastManager.getInstance(getApplicationContext())
      .unregisterReceiver(locationReceiver);
    isLocationReceiverRegistered = false;
  }

  private void disconnectLocationEngine() {
    removeLocationUpdates();
    deactivateLocationEngine();
  }

  private void removeLocationUpdates() {
    locationEngine.removeLocationUpdates();
    locationEngine.removeLocationEngineListener(this);
  }

  private void deactivateLocationEngine() {
    locationEngine.deactivate();
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
      if (MapboxTelemetry.applicationContext == null) {
        Log.d("Null Context", NULL_APPLICATION_CONTEXT);
        return false;
      }

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
