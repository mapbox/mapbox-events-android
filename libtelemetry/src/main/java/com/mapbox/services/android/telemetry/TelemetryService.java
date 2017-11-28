package com.mapbox.services.android.telemetry;


import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.mapbox.services.android.core.location.LocationEngine;
import com.mapbox.services.android.core.location.LocationEngineListener;
import com.mapbox.services.android.core.location.LocationEnginePriority;
import com.mapbox.services.android.core.location.LocationEngineProvider;

import static com.mapbox.services.android.telemetry.LocationReceiver.LOCATION_RECEIVER_INTENT;
import static com.mapbox.services.android.telemetry.TelemetryReceiver.TELEMETRY_RECEIVER_INTENT;

public class TelemetryService extends Service implements TelemetryCallback, LocationEngineListener {
  private LocationReceiver locationReceiver = null;
  private TelemetryReceiver telemetryReceiver = null;
  // For testing only:
  private boolean isLocationReceiverRegistered = false;
  private boolean isTelemetryReceiverRegistered = false;
  private LocationEngine locationEngine = null;
  private @LocationEnginePriority.PowerMode int locationPriority = LocationEnginePriority.HIGH_ACCURACY;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    // A service may return null if clients can not bind to the service (preferred case)
    // For testing purposes a new telemetry binder is returned
    return new TelemetryBinder();
  }

  @Override
  public void onCreate() {
    // TODO Check for location permissions here
    createLocationReceiver();
    createTelemetryReceiver();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    // TODO If the location receiver was not created (permissions were not granted) do not unregister
    unregisterLocationReceiver();
    unregisterTelemetryReceiver();
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
    LocalBroadcastManager.getInstance(this).sendBroadcast(LocationReceiver.supplyIntent(location));
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
    locationReceiver = new LocationReceiver();
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

  public void updateSessionIdentifier(SessionIdentifier sessionIdentifier) {
    locationReceiver.updateSessionIdentifier(sessionIdentifier);
  }

  class TelemetryBinder extends Binder {
    TelemetryService obtainService() {
      return TelemetryService.this;
    }
  }

  public void updateLocationPriority(@LocationEnginePriority.PowerMode int priority) {
    locationPriority = priority;

    if (locationEngine != null) {
      disconnectLocationEngine();
      locationEngine.setPriority(locationPriority);
      locationEngine.activate();
    }
  }
}
