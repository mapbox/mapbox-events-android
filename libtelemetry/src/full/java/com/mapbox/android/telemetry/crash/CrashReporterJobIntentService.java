package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import com.mapbox.android.core.FileUtils;
import com.mapbox.android.telemetry.CrashEvent;

import java.io.File;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_TELEMETRY_PACKAGE;

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
    try {
      File rootDirectory = FileUtils.getFile(getApplicationContext(), MAPBOX_TELEMETRY_PACKAGE);
      if (!rootDirectory.exists()) {
        Log.w(LOG_TAG, "Root directory doesn't exist");
        return;
      }

      CrashReporterClient client = CrashReporterClient
        .create(getApplicationContext())
        .loadFrom(rootDirectory);

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

        if (client.send(event)) {
          client.delete(event);
        } else {
          Log.w(LOG_TAG, "Failed to deliver crash event");
        }
      }
    } catch (Throwable throwable) {
      // TODO: log silent crash
    }
  }
}
