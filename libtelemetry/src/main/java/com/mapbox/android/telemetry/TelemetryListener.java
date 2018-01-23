package com.mapbox.android.telemetry;

interface TelemetryListener {

  void onHttpResponse(boolean successful, int code);

  void onHttpFailure(String message);
}
