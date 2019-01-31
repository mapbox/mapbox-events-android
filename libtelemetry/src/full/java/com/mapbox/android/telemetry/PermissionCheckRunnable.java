package com.mapbox.android.telemetry;

import android.content.Context;
import android.os.Handler;

import com.mapbox.android.core.permissions.PermissionsManager;

class PermissionCheckRunnable implements Runnable {
  private final Context context;
  private final Handler handler = new Handler();
  private final ExponentialBackoff counter = new ExponentialBackoff();
  private MapboxTelemetry mapboxTelemetry;

  PermissionCheckRunnable(Context context, MapboxTelemetry mapboxTelemetry) {
    this.context = context;
    this.mapboxTelemetry = mapboxTelemetry;
  }

  @Override
  public void run() {
    if (PermissionsManager.areLocationPermissionsGranted(context)) {
      mapboxTelemetry.optLocationIn();
    } else {
      long nextWaitTime = counter.nextBackOffMillis();
      handler.postDelayed(this, nextWaitTime);
    }
  }
}
