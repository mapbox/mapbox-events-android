package com.mapbox.android.core.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.location.LocationResult;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FusedLocationIntentService extends IntentService {

  static final String ACTION_PROCESS_UPDATES =
    "com.mapbox.android.core.location.FusedLocationIntentService.ACTION_PROCESS_UPDATES";
  private static CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners;

  public FusedLocationIntentService() {
    super("FusedLocationIntentService");
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    if (intent != null) {
      final String action = intent.getAction();
      if (ACTION_PROCESS_UPDATES.equals(action)) {
        LocationResult result = LocationResult.extractResult(intent);
        if (result != null) {
          List<Location> locations = result.getLocations();
          if (!locations.isEmpty()) {
            Location location = locations.get(0);

            for (LocationEngineListener listener : locationEngineListeners) {
              listener.onLocationChanged(location);
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
