package com.mapbox.android.telemetry;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import static com.mapbox.android.telemetry.JobSchedulerFlusher.START_JOB_INTENT_KEY;
import static com.mapbox.android.telemetry.JobSchedulerFlusher.STOP_JOB_INTENT_KEY;
import static com.mapbox.android.telemetry.SchedulerFlusherFactory.SCHEDULER_FLUSHER_INTENT;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class SchedulerFlusherJobService extends JobService {
  private static final String ON_START_INTENT_EXTRA = "onStart";
  private static final String ON_ERROR_INTENT_EXTRA = "onError";

  @Override
  public boolean onStartJob(final JobParameters params) {
    Intent intent = new Intent(SCHEDULER_FLUSHER_INTENT);
    intent.putExtra(START_JOB_INTENT_KEY, ON_START_INTENT_EXTRA);
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    return false;
  }

  @Override
  public boolean onStopJob(final JobParameters params) {
    Intent intent = new Intent(SCHEDULER_FLUSHER_INTENT);
    intent.putExtra(STOP_JOB_INTENT_KEY, ON_ERROR_INTENT_EXTRA);
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    return true;
  }
}
