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
import okhttp3.OkHttpClient;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationJobService extends JobService implements LocationListener, okhttp3.Callback {
  private final String LOG_TAG = "JobService";
  private static final int JOB_ID = 1;
  private static final int FIVE_MIN = 60 * 1000 * 5;
  private LocationManager gpsLocationManager;
  private LocationManager wifiLocationManager;
  private JobParameters currentParams;
  private ArrayList<Location> locations;
  private String accessToken;
  private String userAgent;
  private Location lastLocation;
  private int radius;
  private int gpsCount;

  @RequiresApi(api = Build.VERSION_CODES.N)
  public static void schedule(Context context, String userAgent, String accessToken, Location lastLocation) {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putString("userAgent", userAgent);
    bundle.putString("accessToken", accessToken);
    bundle.putDouble("latitude", lastLocation.getLatitude());
    bundle.putDouble("longitude", lastLocation.getLongitude());

    ComponentName component = new ComponentName(context, LocationJobService.class);
    JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component)
      .setPeriodic(FIVE_MIN, 2 * FIVE_MIN)
      .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
      .setExtras(bundle);

    JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    if (jobScheduler != null) {
      jobScheduler.schedule(builder.build());
    }
  }

  @Override
  public boolean onStartJob(JobParameters params) {
    currentParams = params;
    locations = new ArrayList<Location>();
    radius = 100;
    gpsCount = 0;
    userAgent = params.getExtras().getString("userAgent");
    accessToken = params.getExtras().getString("accessToken");
    lastLocation = new Location("lastLoc");
    lastLocation.setLatitude(params.getExtras().getDouble("latitude"));
    lastLocation.setLongitude(params.getExtras().getDouble("longitude"));

    Log.d(LOG_TAG,"start job");

    checkDistanceFromLastLocation();

    return true;
  }

  @SuppressLint("MissingPermission")
  private void checkDistanceFromLastLocation() {
    Log.d(LOG_TAG,"checkDistanceFromLastLocation");

    LocationListener initialListener = new LocationListener() {
      @SuppressLint("NewApi")
      @Override
      public void onLocationChanged(Location location) {
        wifiLocationManager.removeUpdates(this);

        float distance = lastLocation.distanceTo(location);

        if (distance >= radius) {
          Log.d(LOG_TAG,"collect Location Data");
          startGps(20000);
        } else {
          Log.d(LOG_TAG,"distance too close, reschedule job, stop current one");
          LocationJobService.schedule(getApplicationContext(), userAgent, accessToken, lastLocation);
          jobFinished(currentParams, false);
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
    };

    wifiLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    wifiLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.0f, initialListener);
  }

  @SuppressLint("MissingPermission")
  private void startGps(long gpsMillis) {
    Log.d(LOG_TAG,"startGPS");
    Criteria criteria = new Criteria();
    criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
    criteria.setPowerRequirement(Criteria.POWER_HIGH);

    gpsLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    if (gpsLocationManager != null) {
      gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, this);
    }

    final LocationListener locationListener = this;

    new CountDownTimer(gpsMillis, 2000) {
      public void onTick(long millisUntilFinished) {

      }

      @RequiresApi(api = Build.VERSION_CODES.N)
      public void onFinish() {
        Log.d(LOG_TAG,"gpsTimer finished");
        gpsLocationManager.removeUpdates(locationListener);
        gpsCount++;

        if (gpsCount == 2) {
          sendLocation(locations);
        } else {
          startWifiListener();
        }
      }
    }.start();
  }

  @SuppressLint("MissingPermission")
  private void startWifiListener() {
    wifiLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    wifiLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.0f, this);

    final LocationListener locationListener = this;

    new CountDownTimer(25000, 10000) {
      public void onTick(long millisUntilFinished) {

      }

      public void onFinish() {
        wifiLocationManager.removeUpdates(locationListener);
        startGps(10000);
      }
    }.start();
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    Log.d(LOG_TAG,"stop job");
    return false;
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onLocationChanged(Location location) {
    Log.d(LOG_TAG,"location: " + location);

    locations.add(location);
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
    jobFinished(currentParams, true);
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  public void onResponse(Call call, Response response) throws IOException {
    Log.d(LOG_TAG,"job finished");
    LocationJobService.schedule(getApplicationContext(), userAgent, accessToken, lastLocation);
    jobFinished(currentParams, false);
  }

  private TelemetryClient createTelemetryClient() {
    ConfigurationClient configurationClient = new ConfigurationClient(getApplicationContext(), TelemetryUtils.createFullUserAgent(userAgent,
      getApplicationContext()), accessToken, new OkHttpClient());
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(accessToken, userAgent,
      new Logger(), new CertificateBlacklist(this, configurationClient));
    TelemetryClient telemetryClient = telemetryClientFactory.obtainTelemetryClient(getApplicationContext());
    return telemetryClient;
  }

  private LocationEvent createLocationEvent(Location location) {
    MapboxTelemetry.applicationContext = getApplicationContext();

    double latitudeScaled = round(location.getLatitude());
    double longitudeScaled = round(location.getLongitude());
    double longitudeWrapped = wrapLongitude(longitudeScaled);

    LocationEvent locationEvent = new LocationEvent("JobScheduler", latitudeScaled, longitudeWrapped,
      TelemetryUtils.obtainApplicationState(getApplicationContext()));
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

  @RequiresApi(api = Build.VERSION_CODES.N)
  private void sendLocation(List<Location> locations) {

    final List<Event> events = new ArrayList<>(locations.size());

    for (Location location : locations) {
      LocationEvent locationEvent = createLocationEvent(location);
      events.add(locationEvent);
    }

    if (events.size() > 0) {
      lastLocation = locations.get(locations.size() - 1);
      final Callback callback = this;

      new Thread(new Runnable() {
        public void run() {
          TelemetryClient telemetryClient = createTelemetryClient();
          telemetryClient.sendEvents(events, callback);
        }
      }).start();
    } else {
      LocationJobService.schedule(getApplicationContext(), userAgent, accessToken, lastLocation);
      jobFinished(currentParams, false);
    }
  }

}
