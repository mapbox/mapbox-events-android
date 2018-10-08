package com.mapbox.android.core.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
  static final String ACTION_PROCESS_UPDATES = LocationUpdatesBroadcastReceiver.class.getSimpleName();

  private static final String TAG = "LUBroadcastReceiver";
  private final IntentHandler intentHandler;

  LocationUpdatesBroadcastReceiver(IntentHandler intentHandler) {
    this.intentHandler = intentHandler;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent == null) {
      Log.e(TAG, "Intent == null");
      return;
    }

    final String action = intent.getAction();
    if (ACTION_PROCESS_UPDATES.equals(action)) {
      intentHandler.handle(intent);
    }
  }
}