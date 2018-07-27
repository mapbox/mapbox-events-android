package com.mapbox.android.core.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;

public class LocationBroadcastReceiver extends BroadcastReceiver {

  static final String ACTION_PROCESS_UPDATES = "com.mapbox.mapboxsdk.context.ACTION_PROCESS_UPDATES";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null) {
      final String action = intent.getAction();
      if (ACTION_PROCESS_UPDATES.equals(action)) {
        LocationResult result = LocationResult.extractResult(intent);
        if (result != null) {
          List<Location> locations = result.getLocations();
          if(!locations.isEmpty()) {
            for (Location location : locations) {
              Log.e("test", "Location update: " + location.toString());
            }

          }
        }
      }
    }
  }
}