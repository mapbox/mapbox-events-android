package com.mapbox.android.telemetry;

import android.content.Context;
import android.os.Handler;

public class ForegroundCheckRunnable implements Runnable {
  private final Context context;
  private final Handler handler = new Handler();
  private final ExponentialBackoff counter = new ExponentialBackoff();
  private MapboxTelemetry mapboxTelemetry;

  ForegroundCheckRunnable(Context context, MapboxTelemetry mapboxTelemetry) {
    this.context = context;
    this.mapboxTelemetry = mapboxTelemetry;
  }

  @Override
  public void run() {
    if (mapboxTelemetry.isAppInForeground(context)) {
      mapboxTelemetry.optLocationIn();
    } else {
      long nextWaitTime = counter.nextBackOffMillis();
      handler.postDelayed(this, nextWaitTime);
    }
  }
}
