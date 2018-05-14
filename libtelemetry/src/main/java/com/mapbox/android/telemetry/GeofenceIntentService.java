package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class GeofenceIntentService extends IntentService implements Callback {
  private String accessToken;
  private String userAgent;
  private ArrayList<Location> locations;
  private boolean locationOn;
  private GeofenceManager geofenceManager;
  private FusedLocationProviderClient fusedLocationClient;

  /**
   * Creates an IntentService.  Invoked by your subclass's constructor.
   */
  public GeofenceIntentService() {
    super("GeofenceService");
  }

  //dwell = 4
  //enter = 1
  //exit = 2
  @SuppressLint("MissingPermission")
  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    Log.e("Geofence Intent", "Geofence triggered");
    GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
    accessToken = intent.getStringExtra("accessToken");
    userAgent = intent.getStringExtra("userAgent");

    if (geofencingEvent.hasError()) {
      int errorCode = geofencingEvent.getErrorCode();
      Log.e("Geofence Intent", "Geofence Error: " + errorCode);
      return;
    }

    // Get the transition type.
    int geofenceTransition = geofencingEvent.getGeofenceTransition();
    Location geofenceLocation = geofencingEvent.getTriggeringLocation();
    List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

    if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
      Log.e("Geofence Intent", "Geofence exit transition");
      //kill old geofence
      Activity activity = new Activity();
      geofenceManager = new GeofenceManager(getApplicationContext(), activity);

      //start location collection
      locations = new ArrayList<Location>();
      locationOn = true;

      final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
          if (locationResult == null) {
            return;
          }
          for (Location location : locationResult.getLocations()) {
            Log.e("Geofence Intent", "Location received: " + location);
            locations.add(location);

            if (!locationOn) {
              sendLocation(locations);

              //generate new geofence
              geofenceManager.addGeofence(location);
            }
          }
        }
      };

      fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
      LocationRequest locationRequest = LocationRequest.create();
      locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      locationRequest.setInterval(1000);
      locationRequest.setFastestInterval(1000);

      fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

      new CountDownTimer(20000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
          Log.e("Geofence Intent", "timer finished");
          locationOn = false;
          fusedLocationClient.removeLocationUpdates(locationCallback);
        }
      };

    }
  }

  private void sendLocation(List<Location> locations) {
    Log.e("Geofence Intent", "send location");
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

  private LocationEvent createLocationEvent(Location location) {
    MapboxTelemetry.applicationContext = getApplicationContext();

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

  private TelemetryClient createTelemetryClient() {
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(accessToken, userAgent,
      new Logger());
    TelemetryClient telemetryClient = telemetryClientFactory.obtainTelemetryClient(getApplicationContext());
    return telemetryClient;
  }

  @Override
  public void onFailure(Call call, IOException e) {

  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    Log.e("Geofence Intent", "Response: " + response);
  }
}
