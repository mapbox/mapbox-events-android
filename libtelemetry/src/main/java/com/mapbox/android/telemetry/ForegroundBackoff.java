package com.mapbox.android.telemetry;

import android.os.Handler;

class ForegroundBackoff {
  private final Handler handler = new Handler();
  private final ExponentialBackoff counter = new ExponentialBackoff();
  private MapboxTelemetry mapboxTelemetry;

  ForegroundBackoff(MapboxTelemetry mapboxTelemetry) {
    this.mapboxTelemetry = mapboxTelemetry;
  }

  Thread start() {
    Thread thread = new Thread() {
      @Override
      public void run() {
        if (mapboxTelemetry.isAppInForeground()) {
          mapboxTelemetry.optLocationIn();
          Thread.currentThread().interrupt();
        } else {
          long nextWaitTime = counter.nextBackOffMillis();
          handler.postDelayed(this, nextWaitTime);
        }
      }
    };

    thread.start();

    return thread;
  }
}
