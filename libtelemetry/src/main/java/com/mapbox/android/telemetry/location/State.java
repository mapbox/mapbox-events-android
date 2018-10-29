package com.mapbox.android.telemetry.location;

interface State {
  State handleEvent(Event event) throws IllegalStateException;

  int getType();
}
