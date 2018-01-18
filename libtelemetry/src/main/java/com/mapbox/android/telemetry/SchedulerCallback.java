package com.mapbox.android.telemetry;

interface SchedulerCallback {

  void onPeriodRaised();

  void onError();
}
