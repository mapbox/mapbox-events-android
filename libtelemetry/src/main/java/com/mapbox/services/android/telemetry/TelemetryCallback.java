package com.mapbox.services.android.telemetry;


interface TelemetryCallback {

  void onBackground();

  void onForeground();
}
