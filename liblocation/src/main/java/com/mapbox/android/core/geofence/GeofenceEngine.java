package com.mapbox.android.core.geofence;

import android.app.PendingIntent;

public interface GeofenceEngine {
  void addGeofences(GeofenceRequest request, PendingIntent pendingIntent);

  void removeGeofences(PendingIntent pendingIntent);
}
