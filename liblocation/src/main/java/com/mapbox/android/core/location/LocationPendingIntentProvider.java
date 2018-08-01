package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.CopyOnWriteArrayList;

class LocationPendingIntentProvider {
  private static final int REQUEST_CODE = 0;
  private Context context;
  private SdkChecker sdkChecker;
  private CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners;

  LocationPendingIntentProvider(Context context, SdkChecker sdkChecker,
                                CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners) {
    this.context = context;
    this.sdkChecker = sdkChecker;
    this.locationEngineListeners = locationEngineListeners;
  }

  LocationPendingIntent buildLocationPendingIntent() {
    if (sdkChecker.isOreoOrAbove()) {
      Intent intent = new Intent(context, LocationUpdateBroadcastReceiver.class);
      intent.setAction(LocationUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES);

      LocationIntentHandler intentHandler = new LocationIntentHandler(locationEngineListeners);
      LocationUpdateBroadcastReceiver.addIntentHandler(intentHandler);

      PendingIntent broadcastPendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT
      );
      return new LocationBroadcastPendingIntent(broadcastPendingIntent);
    } else {
      Intent intent = new Intent(context, LocationUpdateIntentService.class);
      intent.setAction(LocationUpdateIntentService.ACTION_PROCESS_UPDATES);

      LocationIntentHandler intentHandler = new LocationIntentHandler(locationEngineListeners);
      LocationUpdateIntentService.addIntentHandler(intentHandler);

      PendingIntent servicePendingIntent = PendingIntent.getService(
        context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT
      );
      return new LocationServicePendingIntent(servicePendingIntent);
    }
  }
}
