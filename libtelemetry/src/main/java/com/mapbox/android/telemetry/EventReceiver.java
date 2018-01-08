package com.mapbox.android.telemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


class EventReceiver extends BroadcastReceiver {
  private static final String EVENT_RECEIVED_INTENT_KEY = "event_received";
  private static final String ON_EVENT_INTENT_EXTRA = "onEvent";
  private static final String EVENT_INTENT_KEY = "event";
  static final String EVENT_RECEIVER_INTENT = "com.mapbox.event_receiver";
  private EventCallback callback = null;

  EventReceiver() {
  }

  EventReceiver(EventCallback callback) {
    this.callback = callback;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    String eventReceived = intent.getStringExtra(EVENT_RECEIVED_INTENT_KEY);
    if (ON_EVENT_INTENT_EXTRA.equals(eventReceived)) {
      if (callback != null) {
        Event event = intent.getExtras().getParcelable(EVENT_INTENT_KEY);
        callback.onEventReceived(event);
      }
    }
  }

  public Intent supplyIntent(Event event) {
    Intent eventIntent = new Intent(EVENT_RECEIVER_INTENT);
    eventIntent.putExtra(EVENT_RECEIVED_INTENT_KEY, ON_EVENT_INTENT_EXTRA);
    eventIntent.putExtra(EVENT_INTENT_KEY, event);
    return eventIntent;
  }
}