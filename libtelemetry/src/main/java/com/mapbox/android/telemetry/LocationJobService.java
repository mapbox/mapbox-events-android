package com.mapbox.android.telemetry;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationJobService extends JobService {
  private static final int JOB_ID = 1;
  private static final int ONE_MIN = 60 * 1000;

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public static void schedule(Context context) {
    ComponentName component = new ComponentName(context, LocationJobService.class);
    JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component)
        .setMinimumLatency(ONE_MIN)
        .setOverrideDeadline(5 * ONE_MIN);

    JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    jobScheduler.schedule(builder.build());
  }

  @Override
  public boolean onStartJob(JobParameters params) {
    //run work here
    return true;
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    //need to setup retry code here
    return false;
  }
}
