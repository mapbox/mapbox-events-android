package com.mapbox.android.telemetry;


interface TelemetryCallback {

  void onBackground();

  void onForeground();
}
