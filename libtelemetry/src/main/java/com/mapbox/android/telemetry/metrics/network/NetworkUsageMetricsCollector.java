package com.mapbox.android.telemetry.metrics.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.mapbox.android.telemetry.metrics.TelemetryMetrics;

public class NetworkUsageMetricsCollector {
  private static final int TYPE_NONE = -1;
  private final ConnectivityManager connectivityManager;
  private final TelemetryMetrics metrics;

  public NetworkUsageMetricsCollector(Context context, TelemetryMetrics metrics) {
    this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    this.metrics = metrics;
  }

  void addRxBytes(long bytes) {
    metrics.addRxBytesForType(getActiveNetworkType(), bytes);
  }

  void addTxBytes(long bytes) {
    metrics.addTxBytesForType(getActiveNetworkType(), bytes);
  }

  private int getActiveNetworkType() {
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    return activeNetwork == null ? TYPE_NONE : activeNetwork.getType();
  }
}
