package com.mapbox.android.core.location;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.util.Log;

public class FusedLocationIntentService extends IntentService {

  public FusedLocationIntentService() {
    super("FusedLocationIntentService");
    Log.e("test", "FusedLocationIntentService");
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    Log.e("test", "here");
    Log.e("test", "received intent: " + intent);
    Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);

    if (location != null) {
      Log.e("test", "onHandleIntent " + location.getLatitude() + "," + location.getLongitude());
    }
  }

  public static PendingIntent getPendingIntent(Context context) {
    // Note: for apps targeting API level 25 ("Nougat") or lower, either
    // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
    // location updates. For apps targeting API level O, only
    // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
    // started in the background in "O".
    Intent intent = new Intent(context, LocationBroadcastReceiver.class);
    intent.setAction(LocationBroadcastReceiver.ACTION_PROCESS_UPDATES);
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
