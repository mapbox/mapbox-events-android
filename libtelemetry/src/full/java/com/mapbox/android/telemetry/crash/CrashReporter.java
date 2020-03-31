package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.mapbox.android.core.FileUtils;
import com.mapbox.android.telemetry.CrashEvent;

import java.io.File;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_TELEMETRY_PACKAGE;

public final class CrashReporter {
  private static final String LOG_TAG = "CrashReporter";

  private Context context;

  public CrashReporter(@NonNull Context context) {
    this.context = context;
  }

  public void sendErrorReports() {
    if (context == null || context.getApplicationContext() == null) {
      return;
    }

    File rootDirectory = FileUtils.getFile(context.getApplicationContext(), MAPBOX_TELEMETRY_PACKAGE);
    if (!rootDirectory.exists()) {
      Log.w(LOG_TAG, "Root directory doesn't exist");
      return;
    }

    handleCrashReports(CrashReporterClient
      .create(context.getApplicationContext())
      .loadFrom(rootDirectory));
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
