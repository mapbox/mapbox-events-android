package com.mapbox.android.telemetry.location;

interface Timer {
  void start(long timeout);

  void cancel();

  interface Callback {
    void onExpired();
  }
}
