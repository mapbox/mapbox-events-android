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
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.math.BigDecimal;
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
  private ArrayList<Location> gpsLocations;
  private ArrayList<Location> wifiLocations;
  private boolean gpsOn;
  private String accessToken;
  private String userAgent;

  @RequiresApi(api = Build.VERSION_CODES.N)
  public static void schedule(Context context, String userAgent, String accessToken) {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putString("userAgent", userAgent);
    bundle.putString("accessToken", accessToken);

    ComponentName component = new ComponentName(context, LocationJobService.class);
    JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component)
        .setPeriodic(FIVE_MIN, 2 * FIVE_MIN)
        .setPersisted(true)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .setExtras(bundle);

    JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    if (jobScheduler != null) {
      jobScheduler.schedule(builder.build());
    }
  }

  @SuppressLint("MissingPermission")
  @Override
  public boolean onStartJob(JobParameters params) {
    currentParams = params;
    gpsLocations = new ArrayList<Location>();
    wifiLocations = new ArrayList<Location>();
    gpsOn = true;
    userAgent = params.getExtras().getString("userAgent");
    accessToken = params.getExtras().getString("accessToken");

    final LocationListener locationListener = this;
    Log.d(LOG_TAG,"start job");
    Criteria criteria = new Criteria();
    criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
    criteria.setPowerRequirement(Criteria.POWER_HIGH);

    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    if (locationManager != null) {
      locationManager.getBestProvider(criteria, true);
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, this);
    }

    new CountDownTimer(25000, 1000) {
      public void onTick(long millisUntilFinished) {

      }

      public void onFinish() {
        if (gpsOn) {
          locationManager.removeUpdates(locationListener);
          gpsOn = false;
          locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.0f, locationListener);
        }
      }
    }.start();

    return true;
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    Log.d(LOG_TAG,"stop job");
    jobFinished(currentParams, false);
    return false;
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onLocationChanged(Location location) {
    Log.d(LOG_TAG,location.getLatitude() + ", " + location.getLongitude());

    if (gpsOn) {
      gpsLocations.add(location);

      if (gpsLocations.size() == 5) {
        locationManager.removeUpdates(this);
        sendLocation(gpsLocations);
      }
    } else {
      if (location.getAccuracy() > 50) {
        return;
      }

      wifiLocations.add(location);

      if (wifiLocations.size() == 5) {
        locationManager.removeUpdates(this);
        sendLocation(wifiLocations);
      }
    }
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

  @Override
  public void onFailure(Call call, IOException e) {
    Log.d(LOG_TAG,"call failed");
    jobFinished(currentParams, false);
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    Log.d(LOG_TAG,"job finished");
    jobFinished(currentParams, false);
  }

  private TelemetryClient createTelemetryClient() {
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(accessToken, userAgent,
      new Logger());
    TelemetryClient telemetryClient = telemetryClientFactory.obtainTelemetryClient(getApplicationContext());
    return telemetryClient;
  }

  private LocationEvent createLocationEvent(Location location) {
    double latitudeScaled = round(location.getLatitude());
    double longitudeScaled = round(location.getLongitude());
    double longitudeWrapped = wrapLongitude(longitudeScaled);

    LocationEvent locationEvent = new LocationEvent("JobScheduler", latitudeScaled, longitudeWrapped);
    locationEvent.setAccuracy((float) Math.round(location.getAccuracy()));
    locationEvent.setAltitude((double) Math.round(location.getAltitude()));

    return locationEvent;
  }

  private double round(double value) {
    return new BigDecimal(value).setScale(7, BigDecimal.ROUND_DOWN).doubleValue();
  }

  private double wrapLongitude(double longitude) {
    double wrapped = longitude;
    if ((longitude < -180) || (longitude > 180)) {
      wrapped = wrap(longitude, -180, 180);
    }
    return wrapped;
  }

  private double wrap(double value, double min, double max) {
    double delta = max - min;

    double firstMod = (value - min) % delta;
    double secondMod = (firstMod + delta) % delta;

    return secondMod + min;
  }

  private void sendLocation(List<Location> locations) {
    gpsOn = false;

    final List<Event> events = new ArrayList<>(locations.size());

    for (Location location : locations) {
      LocationEvent locationEvent = createLocationEvent(location);
      events.add(locationEvent);
    }

    final Callback callback = this;

    new Thread(new Runnable() {
      public void run() {
        TelemetryClient telemetryClient = createTelemetryClient();
        telemetryClient.sendEvents(events, callback);
      }
    }).start();


  }
}
