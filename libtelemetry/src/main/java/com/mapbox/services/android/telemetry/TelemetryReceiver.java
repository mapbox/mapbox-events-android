package com.mapbox.services.android.telemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


class TelemetryReceiver extends BroadcastReceiver {
  private static final String BACKGROUND_RECEIVED_INTENT_KEY = "background_received";
  private static final String ON_BACKGROUND_INTENT_EXTRA = "onBackground";
  private static final String FOREGROUND_RECEIVED_INTENT_KEY = "foreground_received";
  private static final String ON_FOREGROUND_INTENT_EXTRA = "onForeground";
  static final String TELEMETRY_RECEIVER_INTENT = "com.mapbox.telemetry_receiver";
  private final TelemetryCallback callback;

  TelemetryReceiver(TelemetryCallback callback) {
    this.callback = callback;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    String background = intent.getStringExtra(BACKGROUND_RECEIVED_INTENT_KEY);
    if (ON_BACKGROUND_INTENT_EXTRA.equals(background)) {
      if (callback != null) {
        callback.onBackground();
      }
    }
    String foreground = intent.getStringExtra(FOREGROUND_RECEIVED_INTENT_KEY);
    if (ON_FOREGROUND_INTENT_EXTRA.equals(foreground)) {
      if (callback != null) {
        callback.onForeground();
      }
    }
  }

  Intent supplyBackgroundIntent() {
    Intent backgroundIntent = new Intent(TELEMETRY_RECEIVER_INTENT);
    backgroundIntent.putExtra(BACKGROUND_RECEIVED_INTENT_KEY, ON_BACKGROUND_INTENT_EXTRA);
    return backgroundIntent;
  }

  Intent supplyForegroundIntent() {
    Intent foregroundIntent = new Intent(TELEMETRY_RECEIVER_INTENT);
    foregroundIntent.putExtra(FOREGROUND_RECEIVED_INTENT_KEY, ON_FOREGROUND_INTENT_EXTRA);
    return foregroundIntent;
  }
}