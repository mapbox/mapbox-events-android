package com.mapbox.android.telemetry.metrics;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.util.concurrent.TimeUnit;

public class TelemetryMetricsClient {
  private static final String TELEMETRY_METRICS_USER_AGENT = "mapbox-android-metrics";
  private static TelemetryMetricsClient telemetryMetricsClient;
  private static final Object lock = new Object();
  private final TelemetryMetrics telemetryMetrics;

  @VisibleForTesting
  TelemetryMetricsClient(@NonNull TelemetryMetrics telemetryMetrics) {
    this.telemetryMetrics = telemetryMetrics;
  }

  public static TelemetryMetricsClient install(@NonNull Context context) {
    Context applicationContext;
    if (context.getApplicationContext() == null) {
      // In shared processes content providers getApplicationContext() can return null.
      applicationContext = context;
    } else {
      applicationContext = context.getApplicationContext();
    }

    // TODO: fetch metrics state file
    TelemetryMetrics metrics = new TelemetryMetrics(TimeUnit.HOURS.toMillis(24));
    synchronized (lock) {
      if (telemetryMetricsClient == null) {
        telemetryMetricsClient = new TelemetryMetricsClient(metrics);
      }
    }
    return telemetryMetricsClient;
  }

  @NonNull
  public static TelemetryMetricsClient getInstance() {
    synchronized (lock) {
      if (telemetryMetricsClient != null) {
        return telemetryMetricsClient;
      } else {
        throw new IllegalStateException("TelemetryMetricsClient is not installed.");
      }
    }
  }

  @NonNull
  public TelemetryMetrics getMetrics() {
    return telemetryMetrics;
  }
}
