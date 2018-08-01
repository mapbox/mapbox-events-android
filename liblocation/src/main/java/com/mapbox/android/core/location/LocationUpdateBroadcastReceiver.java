package com.mapbox.android.core.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationUpdateBroadcastReceiver extends BroadcastReceiver {

  static final String ACTION_PROCESS_UPDATES =
    "com.mapbox.android.core.location.LocationBroadcastReceiver.ACTION_PROCESS_UPDATES";
  private static LocationIntentHandler locationIntentHandler;

  @Override
  public void onReceive(Context context, Intent intent) {
    locationIntentHandler.handle(intent, ACTION_PROCESS_UPDATES);
  }

  static void addIntentHandler(LocationIntentHandler intentHandler) {
    locationIntentHandler = intentHandler;
  }
}