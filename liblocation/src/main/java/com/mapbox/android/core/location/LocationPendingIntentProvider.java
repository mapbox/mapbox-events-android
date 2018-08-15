package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Random;

class LocationPendingIntentProvider {

  static LocationPendingIntent buildIntent(Context context, GoogleLocationEngine googleLocationEngine) {
    Intent intent = new Intent("GoogleLocationEngineBroadcast-" + googleLocationEngine.toString());

    PendingIntent broadcastPendingIntent = PendingIntent.getBroadcast(context, generateRequestCode(), intent, 0);
    return new LocationBroadcastPendingIntent(broadcastPendingIntent);
  }

  private static int generateRequestCode() {
    Random random = new Random();
    return random.nextInt();
  }
}
