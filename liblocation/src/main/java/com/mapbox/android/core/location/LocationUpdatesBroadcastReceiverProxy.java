package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

class LocationUpdatesBroadcastReceiverProxy implements BroadcastReceiverProxy {
  private static final String ACTION_PROCESS_UPDATES =
          "com.mapbox.android.core.location.LocationUpdatesBroadcastReceiver";

  private final Context context;

  LocationUpdatesBroadcastReceiverProxy(Context context) {
    this.context = context;
  }

  @Override
  public BroadcastReceiver createReceiver(final IntentHandler intentHandler) {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent == null) {
          return;
        }

        final String action = intent.getAction();
        if (ACTION_PROCESS_UPDATES.equals(action)) {
          intentHandler.handle(intent);
        }
      }
    };
  }

  @Override
  public void registerReceiver(BroadcastReceiver receiver) {
    context.registerReceiver(receiver, new IntentFilter(ACTION_PROCESS_UPDATES));
  }

  @Override
  public void unregisterReceiver(BroadcastReceiver receiver) {
    context.unregisterReceiver(receiver);
  }

  @Override
  public PendingIntent getPendingIntent() {
    Intent intent = new Intent(ACTION_PROCESS_UPDATES);
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
