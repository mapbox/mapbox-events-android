package com.mapbox.android.telemetry;


import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

class EventSender {
  private final Context context;
  private final EventReceiver eventReceiver;

  EventSender(Context context) {
    this.context = context;
    this.eventReceiver = new EventReceiver();
  }

  boolean send(Event event) {
    return LocalBroadcastManager.getInstance(context).sendBroadcast(eventReceiver.supplyIntent(event));
  }
}
