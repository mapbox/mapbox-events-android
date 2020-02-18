package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mapbox.android.core.FileUtils;
import com.mapbox.android.telemetry.CrashEvent;
import com.mapbox.android.telemetry.MapboxTelemetryConstants;

import java.io.File;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_TELEMETRY_PACKAGE;

public final class CrashReporterWorker extends Worker {
  private static final String LOG_TAG = "CrashReporterWorker";

  public CrashReporterWorker(
    @NonNull Context context,
    @NonNull WorkerParameters params) {
    super(context, params);
  }

  @Override
  public Result doWork() {
    try {
      File rootDirectory = FileUtils.getFile(getApplicationContext(), MAPBOX_TELEMETRY_PACKAGE);
      if (!rootDirectory.exists()) {
        Log.w(LOG_TAG, "Root directory doesn't exist");
        return Result.failure();
      }

      String token = getInputData().getString(MapboxTelemetryConstants.ERROR_REPORT_DATA_KEY);

      if (token == null || token.isEmpty()) {
        return Result.failure();
      }

      handleCrashReports(CrashReporterClient
        .create(getApplicationContext(), token)
        .loadFrom(rootDirectory));
    } catch (Throwable throwable) {
      Log.e(LOG_TAG, throwable.toString());
      return Result.failure();
    }

    return Result.success();
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

  public static OneTimeWorkRequest createWorkRequest(String accessToken) {
    return new OneTimeWorkRequest.Builder(CrashReporterWorker.class)
      .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
      .setInputData(new Data.Builder().putString(MapboxTelemetryConstants.ERROR_REPORT_DATA_KEY, accessToken).build())
      .build();
  }
}