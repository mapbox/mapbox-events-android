package com.mapbox.android.telemetry.location;

import android.location.Location;
import android.support.annotation.NonNull;
import com.google.android.gms.location.GeofencingRequest;
import com.mapbox.android.core.api.BroadcastReceiverProxy;
import com.mapbox.android.core.api.IntentHandler;

/**
 * Keeping this interface package private since it depends on gms classes,
 * to save time on building generic framework for geofece engine, including Mapbox
 * geofence engine which is coming to libcore next year.
 */
interface GeofenceEngine {
  void subscribe();

  void unsubscribe();

  void setBroadCastReceiverProxy(@NonNull BroadcastReceiverProxy proxy, @NonNull IntentHandler intentHandler);

  GeofencingRequest getGeofencingRequest(Location location, float radius);

  void addGeofences(GeofencingRequest request);

  void removeGeofences();
}
