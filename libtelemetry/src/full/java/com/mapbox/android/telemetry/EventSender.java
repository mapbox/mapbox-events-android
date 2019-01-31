package com.mapbox.android.telemetry;


import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

class EventSender {
  private final Context context;

  EventSender(Context context) {
    this.context = context;
  }

  boolean send(Event event) {
    return LocalBroadcastManager.getInstance(context).sendBroadcast(EventReceiver.supplyIntent(event));
  }
}
