package com.mapbox.android.telemetry;


import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;

import java.util.concurrent.CopyOnWriteArraySet;

import static com.mapbox.android.telemetry.LocationReceiver.LOCATION_RECEIVER_INTENT;
import static com.mapbox.android.telemetry.TelemetryReceiver.TELEMETRY_RECEIVER_INTENT;

public class TelemetryService extends Service implements TelemetryCallback, LocationEngineListener, EventCallback {
  public static final String IS_LOCATION_ENABLER_FROM_PREFERENCES = "isLocationEnablerFromPreferences";
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
    boundInstances++;
  }

  void unbindInstance() {
    boundInstances--;
  }

  int obtainBoundInstances() {
    return boundInstances;
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
    activateLocationEngine();
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
      telemetryLocationEnabler = new TelemetryLocationEnabler(true);
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
      if (telemetryLocationEnabler == null) {
        telemetryLocationEnabler = new TelemetryLocationEnabler(true);
      }

      telemetryLocationEnabler.updateTelemetryLocationState(TelemetryLocationEnabler.LocationState.DISABLED);
    }
  }

  private void checkApplicationContext() {
    if (MapboxTelemetry.applicationContext == null) {
      MapboxTelemetry.applicationContext = getApplicationContext();
    }
  }

  class TelemetryBinder extends Binder {
    TelemetryService obtainService() {
      return TelemetryService.this;
    }
  }
}
