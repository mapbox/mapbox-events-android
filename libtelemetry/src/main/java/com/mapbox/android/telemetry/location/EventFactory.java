package com.mapbox.android.telemetry.location;

import android.location.Location;

class EventFactory {
  private EventFactory() {
  }

  static Event createLocationUpdatedEvent(Location location) {
    return new LocationUpdatedEvent(location);
  }

  static Event createGeofenceExiteEvent(Location location) {
    return new Event(EventType.GeofenceExited);
  }

  static Event createTimerExpiredEvent() {
    return new Event(EventType.TimerExpired);
  }

  static Event createForegroundEvent() {
    return new Event(EventType.Foreground);
  }

  static Event createBackgroundEvent() {
    return new Event(EventType.Background);
  }

  static Event createStoppedEvent() {
    return new Event(EventType.Stopped);
  }
}
