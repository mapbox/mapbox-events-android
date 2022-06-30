package com.mapbox.android.telemetry.errors;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * This is a background job that sends error events to the telemetry endpoint
 * at startup.
 */
public final class ErrorReporterWorker extends Worker {
  private static final String LOG_TAG = "ErrorReportWorker";

  public ErrorReporterWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @NonNull
  @Override
  public Result doWork() {
    Log.d(LOG_TAG, "doWork");
    try {
      ErrorReporterEngine.sendReports(getApplicationContext());
    } catch (Throwable throwable) {
      // TODO: log silent crash
      Log.e(LOG_TAG, throwable.toString());
    }
    return Result.success();
  }

  public static OneTimeWorkRequest createWorkRequest() {
    return new OneTimeWorkRequest.Builder(ErrorReporterWorker.class)
      .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
      .build();
  }
}
