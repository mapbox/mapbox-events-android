package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Random;

class LocationPendingIntentProvider {
  private static ArrayList<Integer> requestCodes = new ArrayList<>();

  static LocationPendingIntent buildIntent(Context context, String action) {
    Intent intent = new Intent(action);

    PendingIntent broadcastPendingIntent = PendingIntent.getBroadcast(context, generateRequestCode(), intent, 0);
    return new LocationBroadcastPendingIntent(broadcastPendingIntent);
  }

  private static int generateRequestCode() {
    Random random = new Random();
    int requestCode = random.nextInt();

    while (duplicateCode(requestCode)) {
      requestCode = random.nextInt();
    }

    return requestCode;
  }

  private static boolean duplicateCode(int code) {
    return requestCodes.contains(code);
  }
}
