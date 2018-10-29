package com.mapbox.android.telemetry.location;

import android.location.Location;

class LocationUpdatedEvent extends Event {
  private final Location location;

  LocationUpdatedEvent(Location location) {
    super(EventType.LocationUpdated);
    this.location = location;
  }

  Location getLocation() {
    return location;
  }
}
