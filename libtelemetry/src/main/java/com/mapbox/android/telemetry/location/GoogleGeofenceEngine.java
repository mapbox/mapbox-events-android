package com.mapbox.android.telemetry.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.mapbox.android.core.api.BroadcastReceiverProxy;
import com.mapbox.android.core.api.IntentHandler;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.android.core.Utils.isOnClasspath;

class GoogleGeofenceEngine implements GeofenceEngine {
  private static final String GOOGLE_LOCATION_SERVICES = "com.google.android.gms.location.LocationServices";
  private static final String GOOGLE_API_AVAILABILITY = "com.google.android.gms.common.GoogleApiAvailability";
  private static final String TELEMETRY_GEOFENCE_ID = "TelemetryGeofence";

  private final GeofencingClient geofencingClient;
  private final List<Geofence> geofenceList;

  private BroadcastReceiver broadcastReceiver;
  private BroadcastReceiverProxy broadcastReceiverProxy;

  private boolean isSubscribed;

  private GoogleGeofenceEngine(Context context) {
    geofencingClient = LocationServices.getGeofencingClient(context);
    geofenceList = new ArrayList<>();
  }

  @Nullable
  static GeofenceEngine create(Context context) {
    boolean hasGoogleLocationServices = isOnClasspath(GOOGLE_LOCATION_SERVICES);
    if (isOnClasspath(GOOGLE_API_AVAILABILITY)) {
      // Check Google Play services APK is available and up-to-date on this device
      hasGoogleLocationServices &= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        == ConnectionResult.SUCCESS;
    }
    return hasGoogleLocationServices ? new GoogleGeofenceEngine(context) : null;
  }

  @Override
  public void subscribe() {
    if (isSubscribed) {
      return;
    }
    broadcastReceiverProxy.registerReceiver(broadcastReceiver);
    isSubscribed = true;
  }

  @Override
  public void unsubscribe() {
    if (!isSubscribed) {
      return;
    }
    broadcastReceiverProxy.unregisterReceiver(broadcastReceiver);
    isSubscribed = false;
  }

  @Override
  public void setBroadCastReceiverProxy(@NonNull BroadcastReceiverProxy proxy, @NonNull IntentHandler intentHandler) {
    this.broadcastReceiver = proxy.createReceiver(intentHandler);
    this.broadcastReceiverProxy = proxy;
  }

  @Override
  public GeofencingRequest getGeofencingRequest(Location location, float radius) {
    return new GeofencingRequest.Builder()
      .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
      .addGeofences(getGeofenceList(location, radius))
      .build();
  }

  @Override
  public void addGeofences(GeofencingRequest request) {
    try {
      geofencingClient.addGeofences(request, broadcastReceiverProxy.getPendingIntent());
    } catch (SecurityException se) {
      se.printStackTrace();
    }
  }

  @Override
  public void removeGeofences() {
    geofencingClient.removeGeofences(broadcastReceiverProxy.getPendingIntent());
  }

  private List<Geofence> getGeofenceList(Location location, float radius) {
    geofenceList.clear();
    geofenceList.add(new Geofence.Builder()
      .setRequestId(TELEMETRY_GEOFENCE_ID)
      .setCircularRegion(location.getLatitude(), location.getLongitude(), radius)
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
      .build());
    return geofenceList;
  }
}
