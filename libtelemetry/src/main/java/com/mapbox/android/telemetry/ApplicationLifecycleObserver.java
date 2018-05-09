package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.Intent;

class ApplicationLifecycleObserver implements LifecycleObserver {
  private static TelemetryService telemetryService;
  private Context applicationContext;
  private Intent locationServiceIntent = null;

  ApplicationLifecycleObserver(Context applicationContext) {
    this.applicationContext = applicationContext;
  }

  static void setTelemetryService(TelemetryService telemeService) {
    telemetryService = telemeService;
  }

  @SuppressLint("NewApi")
  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  void onEnterForeground() {
    if (telemetryService != null) {
      locationServiceIntent = new Intent(applicationContext, TelemetryService.class);
      applicationContext.startForegroundService(locationServiceIntent);
    }

  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  void onEnterBackground() {
    if (telemetryService != null) {
      telemetryService.stopForegroundService();
    }
  }
}
