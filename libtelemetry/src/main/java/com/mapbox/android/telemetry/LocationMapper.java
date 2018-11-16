package com.mapbox.android.telemetry;

import android.location.Location;

import java.math.BigDecimal;

class LocationMapper {
  private static final int SEVEN_DIGITS_AFTER_DECIMAL = 7;
  private static final double MIN_LONGITUDE = -180;
  private static final double MAX_LONGITUDE = 180;
  private SessionIdentifier sessionIdentifier;

  LocationMapper() {
    sessionIdentifier = new SessionIdentifier();
  }

  LocationEvent from(Location location, String applicationState) {
    LocationEvent locationEvent = createLocationEvent(location, applicationState);
    return locationEvent;
  }

  void updateSessionIdentifier(SessionIdentifier sessionIdentifier) {
    this.sessionIdentifier = sessionIdentifier;
  }

  private LocationEvent createLocationEvent(Location location, String applicationState) {
    String sessionId = sessionIdentifier.getSessionId();
    double latitudeScaled = round(location.getLatitude());
    double longitudeScaled = round(location.getLongitude());
    double longitudeWrapped = wrapLongitude(longitudeScaled);
    LocationEvent locationEvent = new LocationEvent(sessionId, latitudeScaled, longitudeWrapped, applicationState);
    addAltitudeIfPresent(location, locationEvent);
    addAccuracyIfPresent(location, locationEvent);
    return locationEvent;
  }

  private double round(double value) {
    return new BigDecimal(value).setScale(SEVEN_DIGITS_AFTER_DECIMAL, BigDecimal.ROUND_DOWN).doubleValue();
  }

  private double wrapLongitude(double longitude) {
    double wrapped = longitude;
    if ((longitude < MIN_LONGITUDE) || (longitude > MAX_LONGITUDE)) {
      wrapped = wrap(longitude, MIN_LONGITUDE, MAX_LONGITUDE);
    }
    return wrapped;
  }

  private double wrap(double value, double min, double max) {
    double delta = max - min;

    double firstMod = (value - min) % delta;
    double secondMod = (firstMod + delta) % delta;

    return secondMod + min;
  }

  private void addAltitudeIfPresent(Location location, LocationEvent locationEvent) {
    if (location.hasAltitude()) {
      double altitudeRounded = Math.round(location.getAltitude());
      locationEvent.setAltitude(altitudeRounded);
    }
  }

  private void addAccuracyIfPresent(Location location, LocationEvent locationEvent) {
    if (location.hasAccuracy()) {
      float accuracyRounded = Math.round(location.getAccuracy());
      locationEvent.setAccuracy(accuracyRounded);
    }
  }
}
