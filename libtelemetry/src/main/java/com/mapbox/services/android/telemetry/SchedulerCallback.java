package com.mapbox.services.android.telemetry;

// TODO Access can be package-private, remove public modifier after removing instances from the test app
public interface SchedulerCallback {

  void onPeriodRaised();

  void onError();
}
