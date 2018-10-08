package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

class LocationUpdatesBroadcastReceiverProxy implements BroadcastReceiverProxy<LocationUpdatesBroadcastReceiver> {
  private final Context context;

  LocationUpdatesBroadcastReceiverProxy(Context context) {
    this.context = context;
  }

  @Override
  public LocationUpdatesBroadcastReceiver createReceiver(IntentHandler intentHandler) {
    return new LocationUpdatesBroadcastReceiver(intentHandler);
  }

  @Override
  public void registerReceiver(LocationUpdatesBroadcastReceiver receiver, String action) {
    context.registerReceiver(receiver, new IntentFilter(action));
  }

  @Override
  public void unregisterReceiver(LocationUpdatesBroadcastReceiver receiver) {
    context.unregisterReceiver(receiver);
  }

  @Override
  public PendingIntent getPendingIntent(Class<LocationUpdatesBroadcastReceiver> clazz, String action) {
    Intent intent = new Intent(context, clazz);
    intent.setAction(action);
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
