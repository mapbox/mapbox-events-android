package com.mapbox.android.telemetry;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class LocationIntentService extends IntentService implements Callback {
  private final String LOG_TAG = "IntentService";
  static final String ACTION_PROCESS_UPDATES = "MapboxProcessLocationUpdates";
  private static final String INTENT_SERVICE_NAME = LocationIntentService.class.getName();
  private String userAgent;
  private String accessToken;
  private List<Location> locations;

  public LocationIntentService() {
    super(INTENT_SERVICE_NAME);
  }

  public void setUserAgent(String userAgent) {
    Log.e("test", "setUserAgent");
    this.userAgent = userAgent;
  }

  public void setAccessToken(String accessToken) {
    Log.e("test", "setAccessToken");
    this.accessToken = accessToken;
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.e(LOG_TAG,"onHandleIntent: " + intent);

    if (null == intent) {
      return;
    }

    Bundle bundle = intent.getExtras();

    if (null == bundle) {
      return;
    }

    Location location = bundle.getParcelable("com.google.android.location.LOCATION");

    Log.e(LOG_TAG,"Location: " + location);

    if (null == location) {
      Log.e(LOG_TAG,"Location is null, return called");
      return;
    }

    sendLocation(Arrays.asList(location));
  }

  public PendingIntent getPendingIntent() {
    Intent intent = new Intent(MapboxTelemetry.applicationContext, LocationIntentService.class);
    intent.setAction(LocationIntentService.ACTION_PROCESS_UPDATES);
    return PendingIntent.getService(MapboxTelemetry.applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  @Override
  public void onFailure(Call call, IOException e) {
    Log.d(LOG_TAG,"call failed");
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    Log.d(LOG_TAG,"intent finished");
  }

  private void sendLocation(List<Location> locations) {
    Log.e(LOG_TAG,"sendLocation");
    final List<Event> events = new ArrayList<>(locations.size());

    for (Location location : locations) {
      LocationEvent locationEvent = createLocationEvent(location);
      events.add(locationEvent);
    }

    if (events.size() > 0) {
      final Callback callback = this;

      new Thread(new Runnable() {
        public void run() {
          TelemetryClient telemetryClient = createTelemetryClient();
          telemetryClient.sendEvents(events, callback);
        }
      }).start();
    }
  }

  private LocationEvent createLocationEvent(Location location) {
    MapboxTelemetry.applicationContext = getApplicationContext();

    double latitudeScaled = round(location.getLatitude());
    double longitudeScaled = round(location.getLongitude());
    double longitudeWrapped = wrapLongitude(longitudeScaled);

    LocationEvent locationEvent = new LocationEvent("IntentService", latitudeScaled, longitudeWrapped,
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

  private TelemetryClient createTelemetryClient() {
    Log.e("test", "create telem client");
    String accessTokenTelemetry = obtainAccessToken();
    String userAgentTelemetry = "MapboxEventsAndroid/job-v-intent";

    ConfigurationClient configurationClient = new ConfigurationClient(getApplicationContext(), TelemetryUtils.createFullUserAgent(userAgentTelemetry,
      getApplicationContext()), accessTokenTelemetry, new OkHttpClient());
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(accessTokenTelemetry, userAgentTelemetry,
      new Logger(), new CertificateBlacklist(this, configurationClient));
    TelemetryClient telemetryClient = telemetryClientFactory.obtainTelemetryClient(getApplicationContext());
    return telemetryClient;
  }

  private String obtainAccessToken() {
    String accessToken =  "pk.eyJ1IjoiZWxlY3Ryb3N0YXQtdGVzdCIsImEiOiJjamRhaHBhejkydXhlMnhvNmZhZTk3cjI1In0.9I3NYZF29F-XQHW1JIzIPg";
    return accessToken;
  }
}
