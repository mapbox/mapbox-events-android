package com.mapbox.android.core.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocationBroadcastReceiver extends BroadcastReceiver {

  static final String ACTION_PROCESS_UPDATES =
    "com.mapbox.android.core.location.LocationBroadcastReceiver.ACTION_PROCESS_UPDATES";
  private static CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null) {
      final String action = intent.getAction();
      if (ACTION_PROCESS_UPDATES.equals(action)) {
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

  public static void addListeners(CopyOnWriteArrayList<LocationEngineListener> listeners) {
    locationEngineListeners = listeners;
  }
}