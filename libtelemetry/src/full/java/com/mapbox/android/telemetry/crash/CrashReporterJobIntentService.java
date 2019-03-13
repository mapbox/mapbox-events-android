package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

/**
 * This is a background job that sends crash events to the telemetry endpoint
 * at startup.
 */
public final class CrashReporterJobIntentService extends JobIntentService {
  private static final String LOG_TAG = "CrashJobIntentService";
  private static final int JOB_ID = 666;

  static void enqueueWork(@NonNull Context context) {
    enqueueWork(context, CrashReporterJobIntentService.class, JOB_ID,
      new Intent(context, CrashReporterJobIntentService.class));
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    Log.d(LOG_TAG, "onHandleWork");
    CrashReporterClient client = CrashReporterClient.create(getApplicationContext());
    // Check if crash reporter enabled
    if (!client.isEnabled()) {
      Log.w(LOG_TAG, "Crash reporter is disabled");
      return;
    }

    while (client.hasNextEvent()) {
      CrashEvent event = client.nextEvent();
      if (client.isDuplicate(event)) {
        Log.d(LOG_TAG, "Skip duplicate crash in this batch: " + event.getHash());
        continue;
      }
      client.send(event);
    }
  }
}
