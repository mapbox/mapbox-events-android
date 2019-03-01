package com.mapbox.android.telemetry.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Broadcast receiver through which location updates are reported.
 * This receiver is optimized for the background location updates use case.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = "LocationUpdateReceiver";
  static final String ACTION_LOCATION_UPDATED =
    "com.mapbox.android.telemetry.location.locationupdatespendingintent.action.LOCATION_UPDATED";

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      if (intent == null) {
        Log.w(TAG, "intent == null");
        return;
      }

      final String action = intent.getAction();
      if (ACTION_LOCATION_UPDATED.equals(action)) {
        LocationUpdatesJobIntentService.enqueueWork(context, intent);
      }
    } catch (Throwable throwable) {
      // TODO: log silent crash
    }
  }
}
