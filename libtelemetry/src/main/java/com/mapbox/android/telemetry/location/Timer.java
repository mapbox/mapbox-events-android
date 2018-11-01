package com.mapbox.android.telemetry.location;

interface Timer {
  void start(long timeout);

  void setCallback(Callback callback);

  void cancel();

  interface Callback {
    void onExpired();
  }
}
