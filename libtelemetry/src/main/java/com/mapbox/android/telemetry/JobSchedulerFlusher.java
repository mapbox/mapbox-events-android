package com.mapbox.android.telemetry;


import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import static com.mapbox.android.telemetry.SchedulerFlusherFactory.flushingPeriod;

class JobSchedulerFlusher implements SchedulerFlusher {
  private static final int SCHEDULER_FLUSHER_JOB_ID = 0;
  static final String START_JOB_INTENT_KEY = "start_job";
  static final String STOP_JOB_INTENT_KEY = "stop_job";
  private final Context context;
  private final SchedulerCallback callback;
  private BroadcastReceiver receiver;

  JobSchedulerFlusher(Context context, SchedulerCallback callback) {
    this.context = context;
    this.callback = callback;
  }

  @Override
  public void register() {
    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String start = intent.getStringExtra(START_JOB_INTENT_KEY);
        String stop = intent.getStringExtra(STOP_JOB_INTENT_KEY);
        if (start != null) {
          callback.onPeriodRaised();
        }
        if (stop != null) {
          callback.onError();
        }
      }
    };
    LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
      new IntentFilter(SchedulerFlusherFactory.SCHEDULER_FLUSHER_INTENT));
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void schedule(long elapsedRealTime) {
    JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    jobScheduler.schedule(new JobInfo.Builder(SCHEDULER_FLUSHER_JOB_ID,
      new ComponentName(context, SchedulerFlusherJobService.class))
      .setPeriodic(flushingPeriod)
      .build());
  }

  @Override
  public void unregister() {
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
  }
}
