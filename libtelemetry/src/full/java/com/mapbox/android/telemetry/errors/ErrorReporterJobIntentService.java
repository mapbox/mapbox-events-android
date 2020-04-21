package com.mapbox.android.telemetry.errors;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;


/**
 * This is a background job that sends crash events to the telemetry endpoint
 * at startup.
 */
public final class ErrorReporterJobIntentService extends JobIntentService {
  private static final String LOG_TAG = "CrashJobIntentService";
  private static final int JOB_ID = 666;

  static void enqueueWork(@NonNull Context context) {
    enqueueWork(context, ErrorReporterJobIntentService.class, JOB_ID,
      new Intent(context, ErrorReporterJobIntentService.class));
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    Log.d(LOG_TAG, "onHandleWork");
    try {
      ErrorReporterEngine.sendReports(getApplicationContext());
    } catch (Throwable throwable) {
      // TODO: log silent crash
      Log.e(LOG_TAG, throwable.toString());
    }
  }
}
