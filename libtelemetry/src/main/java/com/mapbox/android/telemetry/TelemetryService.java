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
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
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
  private static final String TAG = "TelemetryService";
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
  @SuppressWarnings( {"MissingPermission"})
  public void onConnected() {
    locationEngine.requestLocationUpdates();
  }

  @Override
  public void onLocationChanged(Location location) {
    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(LocationReceiver.supplyIntent(location));
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
    connectLocationEngine();
    LocalBroadcastManager.getInstance(context)
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

  private void unregisterLocationReceiver(Context context) {
    disconnectLocationEngine();
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
