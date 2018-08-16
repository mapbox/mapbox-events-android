package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Random;

class LocationPendingIntentProvider {
  private static ArrayList<Integer> requestCodes = new ArrayList<>();

  static PendingIntent buildIntent(Context context, String action, int requestCode) {
    Intent intent = new Intent(action);
    requestCodes.add(requestCode);

    PendingIntent broadcastPendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);
    return broadcastPendingIntent;
  }

  static int generateRequestCode() {
    Random random = new Random();
    int requestCode = random.nextInt();

    while (duplicateCode(requestCode)) {
      requestCode = random.nextInt();
    }

    return requestCode;
  }

  static void removeRequestCode(int requestCode) {
    requestCodes.remove(requestCode);
  }

  private static boolean duplicateCode(int code) {
    return requestCodes.contains(code);
  }
}
