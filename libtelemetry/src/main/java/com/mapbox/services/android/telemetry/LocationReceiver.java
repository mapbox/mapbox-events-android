package com.mapbox.services.android.telemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

// TODO Access can be package-private, remove public modifier after removing instances from the test app
public class LocationReceiver extends BroadcastReceiver {
  private static final String LOCATION_RECEIVED_INTENT_KEY = "location_received";
  private static final String ON_LOCATION_INTENT_EXTRA = "onLocation";
  static final String LOCATION_RECEIVER_INTENT = "com.mapbox.location_receiver";
  private EventSender eventSender = null;
  private LocationMapper locationMapper = null;

  // TODO Access can be package-private, remove public modifier after removing instances from the test app
  public LocationReceiver() {
  }

  // For testing only
  LocationReceiver(EventSender sender) {
    this.eventSender = sender;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    String locationReceived = intent.getStringExtra(LOCATION_RECEIVED_INTENT_KEY);
    if (ON_LOCATION_INTENT_EXTRA.equals(locationReceived)) {
      Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
      sendEvent(location, context);
    }
  }

  // TODO Access can be package-private, remove public modifier after removing instances from the test app
  public static Intent supplyIntent(Location location) {
    Intent locationIntent = new Intent(LOCATION_RECEIVER_INTENT);
    locationIntent.putExtra(LOCATION_RECEIVED_INTENT_KEY, ON_LOCATION_INTENT_EXTRA);
    locationIntent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
    return locationIntent;
  }

  private boolean sendEvent(Location location, Context context) {
    if (isThereAnyNaN(location) || isThereAnyInfinite(location)) {
      return false;
    }

    EventSender eventSender = obtainEventSender(context);
    LocationMapper obtainLocationEvent = obtainLocationMapper();
    LocationEvent locationEvent = obtainLocationEvent.from(location);
    eventSender.send(locationEvent);
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

  private EventSender obtainEventSender(Context context) {
    if (eventSender == null) {
      eventSender = new EventSender(context);
    }

    return eventSender;
  }

  private LocationMapper obtainLocationMapper() {
    if (locationMapper == null) {
      locationMapper = new LocationMapper();
    }

    return locationMapper;
  }

  void updateSessionIdentifier(SessionIdentifier sessionIdentifier) {
    locationMapper.updateSessionIdentifier(sessionIdentifier);
  }
}