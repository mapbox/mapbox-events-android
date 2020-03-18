package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.JobIntentService;
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

      handleCrashReports(CrashReporterClient
        .create(getApplicationContext())
        .loadFrom(rootDirectory)
      );
    } catch (Throwable throwable) {
      // TODO: log silent crash
      Log.e(LOG_TAG, throwable.toString());
    }
  }

  @VisibleForTesting
  void handleCrashReports(@NonNull CrashReporterClient client) {
    if (!client.isEnabled()) {
      Log.w(LOG_TAG, "Crash reporter is disabled");
      return;
    }

    while (client.hasNextEvent()) {
      CrashEvent event = client.nextEvent();
      if (client.isDuplicate(event)) {
        Log.d(LOG_TAG, "Skip duplicate crash in this batch: " + event.getHash());
        client.delete(event);
        continue;
      }

      if (client.send(event)) {
        client.delete(event);
      } else {
        Log.w(LOG_TAG, "Failed to deliver crash event");
      }
    }
  }
}
