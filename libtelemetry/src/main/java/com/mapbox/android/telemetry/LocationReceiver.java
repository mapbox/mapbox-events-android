package com.mapbox.android.telemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

class LocationReceiver extends BroadcastReceiver {
  private static final String LOCATION_RECEIVED_INTENT_KEY = "location_received";
  private static final String ON_LOCATION_INTENT_EXTRA = "onLocation";
  static final String LOCATION_RECEIVER_INTENT = "com.mapbox.location_receiver";
  private final EventCallback callback;
  private LocationMapper locationMapper = null;

  LocationReceiver(@NonNull EventCallback callback) {
    this.callback = callback;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    String locationReceived = intent.getStringExtra(LOCATION_RECEIVED_INTENT_KEY);
    if (ON_LOCATION_INTENT_EXTRA.equals(locationReceived)) {
      ArrayList<Location> locations = intent.getParcelableArrayListExtra(LocationManager.KEY_LOCATION_CHANGED);
      for (Location location: locations) {
        sendEvent(location, context);
      }
    }
  }

  static Intent supplyIntent(List<Location> locations) {
    Intent locationIntent = new Intent(LOCATION_RECEIVER_INTENT);
    locationIntent.putExtra(LOCATION_RECEIVED_INTENT_KEY, ON_LOCATION_INTENT_EXTRA);
    locationIntent.putParcelableArrayListExtra(LocationManager.KEY_LOCATION_CHANGED, getListOfLocations(locations));
    return locationIntent;
  }

  private static ArrayList<Location> getListOfLocations(List<Location> locations) {
    ArrayList<Location> locationsList = new ArrayList<>();
    for (Location location: locations) {
      locationsList.add(location);
    }
    return locationsList;
  }

  void updateSessionIdentifier(SessionIdentifier sessionIdentifier) {
    locationMapper.updateSessionIdentifier(sessionIdentifier);
  }

  private boolean sendEvent(Location location, Context context) {
    if (isThereAnyNaN(location) || isThereAnyInfinite(location)) {
      return false;
    }

    LocationMapper obtainLocationEvent = obtainLocationMapper();
    LocationEvent locationEvent = obtainLocationEvent.from(location, TelemetryUtils.obtainApplicationState(context));
    callback.onEventReceived(locationEvent);
    return true;
  }

  private boolean isThereAnyNaN(Location location) {
    return Double.isNaN(location.getLatitude()) || Double.isNaN(location.getLongitude())
      || Double.isNaN(location.getAltitude()) || Float.isNaN(location.getAccuracy());
  }

  private boolean isThereAnyInfinite(Location location) {
    return Double.isInfinite(location.getLatitude()) || Double.isInfinite(location.getLongitude())
      || Double.isInfinite(location.getAltitude()) || Float.isInfinite(location.getAccuracy());
  }

  private LocationMapper obtainLocationMapper() {
    if (locationMapper == null) {
      locationMapper = new LocationMapper();
    }

    return locationMapper;
  }
}