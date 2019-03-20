package com.mapbox.android.telemetry.location;

import android.location.Location;
import com.mapbox.android.telemetry.LocationEvent;

import java.math.BigDecimal;

public class LocationMapper {
  private static final int SEVEN_DIGITS_AFTER_DECIMAL = 7;
  private static final double MIN_LONGITUDE = -180;
  private static final double MAX_LONGITUDE = 180;
  private SessionIdentifier sessionIdentifier;

  public LocationMapper() {
    sessionIdentifier = new SessionIdentifier();
  }

  public static LocationEvent create(Location location, String sessionId) {
    // We don't necessarily want to poke activity manager for every single location
    // update to fetch app state, cause it's extremely expensive to do so on main thread
    // and we're not making use of appState internally.
    // Going forward app state changes will be observed by metrics client and can be
    // correlated with location events via session id.
    // Since api-events won't accept empty string, default state to unknown
    return createLocationEvent(location, "unknown", sessionId);
  }

  public LocationEvent from(Location location, String applicationState) {
    return createLocationEvent(location, applicationState, sessionIdentifier.getSessionId());
  }

  public void updateSessionIdentifier(SessionIdentifier sessionIdentifier) {
    this.sessionIdentifier = sessionIdentifier;
  }

  private static LocationEvent createLocationEvent(Location location, String applicationState, String sessionId) {
    double latitudeScaled = round(location.getLatitude());
    double longitudeScaled = round(location.getLongitude());
    double longitudeWrapped = wrapLongitude(longitudeScaled);
    LocationEvent locationEvent = new LocationEvent(sessionId, latitudeScaled, longitudeWrapped, applicationState);
    addAltitudeIfPresent(location, locationEvent);
    addAccuracyIfPresent(location, locationEvent);
    return locationEvent;
  }

  private static double round(double value) {
    return new BigDecimal(value).setScale(SEVEN_DIGITS_AFTER_DECIMAL, BigDecimal.ROUND_DOWN).doubleValue();
  }

  private static double wrapLongitude(double longitude) {
    double wrapped = longitude;
    if ((longitude < MIN_LONGITUDE) || (longitude > MAX_LONGITUDE)) {
      wrapped = wrap(longitude, MIN_LONGITUDE, MAX_LONGITUDE);
    }
    return wrapped;
  }

  private static double wrap(double value, double min, double max) {
    double delta = max - min;
    double firstMod = (value - min) % delta;
    double secondMod = (firstMod + delta) % delta;
    return secondMod + min;
  }

  private static void addAltitudeIfPresent(Location location, LocationEvent locationEvent) {
    if (location.hasAltitude()) {
      double altitudeRounded = Math.round(location.getAltitude());
      locationEvent.setAltitude(altitudeRounded);
    }
  }

  private static void addAccuracyIfPresent(Location location, LocationEvent locationEvent) {
    if (location.hasAccuracy()) {
      float accuracyRounded = Math.round(location.getAccuracy());
      locationEvent.setAccuracy(accuracyRounded);
    }
  }
}
