package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.CopyOnWriteArrayList;

class LocationPendingIntentProvider {

  private static final int REQUEST_CODE = 0;
  private final LocationPendingIntent locationPendingIntent;

  LocationPendingIntentProvider(Context context, SdkChecker sdkChecker,
                                CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners) {
    locationPendingIntent = buildIntent(context, sdkChecker, locationEngineListeners);
  }

  LocationPendingIntent intent() {
    return locationPendingIntent;
  }

  private LocationPendingIntent buildIntent(Context context, SdkChecker sdkChecker,
                                            CopyOnWriteArrayList<LocationEngineListener> locationListeners) {
    if (sdkChecker.isOreoOrAbove()) {
      Intent intent = new Intent(context, LocationUpdateBroadcastReceiver.class);
      intent.setAction(LocationUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES);

      LocationIntentHandler intentHandler = new LocationIntentHandler(locationListeners);
      LocationUpdateBroadcastReceiver.addIntentHandler(intentHandler);

      PendingIntent broadcastPendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT
      );
      return new LocationBroadcastPendingIntent(broadcastPendingIntent);
    } else {
      Intent intent = new Intent(context, LocationUpdateIntentService.class);
      intent.setAction(LocationUpdateIntentService.ACTION_PROCESS_UPDATES);

      LocationIntentHandler intentHandler = new LocationIntentHandler(locationListeners);
      LocationUpdateIntentService.addIntentHandler(intentHandler);

      PendingIntent servicePendingIntent = PendingIntent.getService(
        context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT
      );
      return new LocationServicePendingIntent(servicePendingIntent);
    }
  }
}
