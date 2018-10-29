package com.mapbox.android.telemetry.location;

class Event {
  @EventType
  private final int type;

  Event(@EventType int type) {
    this.type = type;
  }

  @EventType
  int getType() {
    return type;
  }
}