package com.mapbox.android.core.location;

import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class LocationIntentHandler {
  private CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners;

  LocationIntentHandler(CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners) {
    this.locationEngineListeners = locationEngineListeners;
  }

  void handle(Intent intent, String actionTag) {
    if (intent != null) {
      final String action = intent.getAction();
      if (actionTag.equals(action)) {
        LocationResult result = LocationResult.extractResult(intent);
        if (result != null) {
          List<Location> locations = result.getLocations();
          if (!locations.isEmpty()) {
            for (LocationEngineListener listener : locationEngineListeners) {
              listener.onLocationChanged(locations.get(0));
            }
          }
        }
      }
    }
  }
}
