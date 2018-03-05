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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationJobService extends JobService implements LocationListener, Callback {
  private final String LOG_TAG = "JobService";
  private static final int JOB_ID = 1;
  private static final int FIVE_MIN = 60 * 1000 * 5;
  private LocationManager locationManager;
  private JobParameters currentParams;
  private TelemetryClient telemetryClient;

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

  public void setTelemetryClient(TelemetryClient telemetryClient) {
    Log.d(LOG_TAG,"Telemetry client: " + telemetryClient);
    this.telemetryClient = telemetryClient;
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    Log.d(LOG_TAG,"stop job");
    jobFinished(currentParams, true);
    return false;
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onLocationChanged(Location location) {
    Log.d(LOG_TAG,location.getLatitude() + ", " + location.getLongitude());

    locationManager.removeUpdates(this);

    LocationEvent locationEvent = createLocationEvent(location);

    List<Event> event = new ArrayList<>(1);
    event.add(locationEvent);

    telemetryClient.sendEvents(event, this);
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

  private LocationEvent createLocationEvent(Location location) {
    LocationEvent locationEvent = new LocationEvent("JobScheduler", location.getLatitude(), location.getLongitude());
    locationEvent.setAccuracy(location.getAccuracy());
    locationEvent.setAltitude(location.getAltitude());

    return locationEvent;
  }

  @Override
  public void onFailure(Call call, IOException e) {
    Log.d(LOG_TAG,"call failed");
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    Log.d(LOG_TAG,"call response: " + response);

    jobFinished(currentParams, true);
  }
}
