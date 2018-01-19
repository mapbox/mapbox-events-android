package com.mapbox.services.android.telemetry;

public interface TelemetryListener {

  void onHttpResponse(boolean successful, int code);

  void onHttpFailure(String message);
}
