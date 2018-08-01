package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.CopyOnWriteArrayList;

class LocationPendingIntentProvider {
  private Context context;
  private SdkChecker sdkChecker;
  private CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners;

  LocationPendingIntentProvider(Context context, SdkChecker sdkChecker,
                         CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners) {
    this.context = context;
    this.sdkChecker = sdkChecker;
    this.locationEngineListeners = locationEngineListeners;
  }

  PendingIntent buildPendingIntent() {
    if (sdkChecker.isOreoOrAbove()) {
      Intent intent = new Intent(context, LocationUpdateBroadcastReceiver.class);
      intent.setAction(LocationUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES);

      LocationIntentHandler intentHandler = new LocationIntentHandler(locationEngineListeners);
      LocationUpdateBroadcastReceiver.addIntentHandler(intentHandler);

      return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    } else {
      Intent intent = new Intent(context, LocationUpdateIntentService.class);
      intent.setAction(LocationUpdateIntentService.ACTION_PROCESS_UPDATES);

      LocationIntentHandler intentHandler = new LocationIntentHandler(locationEngineListeners);
      LocationUpdateIntentService.addIntentHandler(intentHandler);

      return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
  }
}
