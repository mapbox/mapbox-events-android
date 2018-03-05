package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationJobService extends JobService implements LocationListener {
  private final String LOG_TAG = "JobService";
  private static final int JOB_ID = 1;
  private static final int FIVE_MIN = 60 * 1000 * 5;
  private LocationManager locationManager;
  private JobParameters currentParams;

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public static void schedule(Context context) {
    ComponentName component = new ComponentName(context, LocationJobService.class);
    JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component)
        .setMinimumLatency(FIVE_MIN)
        .setOverrideDeadline(2 * FIVE_MIN)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

    JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    if (jobScheduler != null) {
      jobScheduler.schedule(builder.build());
    }
  }

  @SuppressLint("MissingPermission")
  @Override
  public boolean onStartJob(JobParameters params) {
    currentParams = params;
    Log.d(LOG_TAG,"start job");
    Criteria criteria = new Criteria();
    criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
    criteria.setPowerRequirement(Criteria.POWER_HIGH);

    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    if (locationManager != null) {
      locationManager.getBestProvider(criteria, true);
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, this);
    }

    return true;
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    Log.d(LOG_TAG,"stop job");
    return false;
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onLocationChanged(Location location) {
    Log.d(LOG_TAG,location.getLatitude() + ", " + location.getLongitude());

    locationManager.removeUpdates(this);
    jobFinished(currentParams, true);
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {

  }

  @Override
  public void onProviderEnabled(String provider) {

  }

  @Override
  public void onProviderDisabled(String provider) {

  }
}
