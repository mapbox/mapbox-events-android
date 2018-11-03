package com.mapbox.android.telemetry.location;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.mapbox.android.core.api.BroadcastReceiverProxy;
import com.mapbox.android.core.api.IntentHandler;

class GeofenceEventBroadcastReceiverProxy implements BroadcastReceiverProxy {
  private static final String ACTION_PROCESS_UPDATES =
    "com.mapbox.android.telemetry.location.GeofenceEventBroadcastReceiverProxy";

  private final Context context;

  GeofenceEventBroadcastReceiverProxy(Context context) {
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
    try {
      context.registerReceiver(receiver, new IntentFilter(ACTION_PROCESS_UPDATES));
    } catch (IllegalArgumentException iae) {
      iae.printStackTrace();
    }
  }

  @Override
  public void unregisterReceiver(BroadcastReceiver receiver) {
    try {
      context.unregisterReceiver(receiver);
    } catch (IllegalArgumentException iae) {
      iae.printStackTrace();
    }
  }

  @Override
  public PendingIntent getPendingIntent() {
    Intent intent = new Intent(ACTION_PROCESS_UPDATES);
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
