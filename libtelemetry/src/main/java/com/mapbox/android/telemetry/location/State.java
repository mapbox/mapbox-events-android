package com.mapbox.android.telemetry.location;

import android.support.annotation.NonNull;

interface State {
  @NonNull
  State handleEvent(Event event) throws IllegalStateException;

  int getType();
}
