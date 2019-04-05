package com.mapbox.android.telemetry.metrics;

import static android.net.ConnectivityManager.TYPE_WIFI;

public class TelemetryMetrics extends AbstractCompositeMetrics {
  public static final String EVENTS_TOTAL = "eventCountTotal";
  public static final String EVENTS_FAILED = "eventCountFailed";

  private static final String MOBILE_BYTES_TX = "cellDataSent";
  private static final String WIFI_BYTES_TX = "wifiDataSent";
  private static final String MOBILE_BYTES_RX = "cellDataReceived";
  private static final String WIFI_BYTES_RX = "wifiDataReceived";

  public TelemetryMetrics(long maxLength) {
    super(maxLength);
  }

  public void addRxBytesForType(int networkType, long bytes) {
    add(networkType == TYPE_WIFI ? WIFI_BYTES_RX : MOBILE_BYTES_RX, bytes);
  }

  public void addTxBytesForType(int networkType, long bytes) {
    add(networkType == TYPE_WIFI ? WIFI_BYTES_TX : MOBILE_BYTES_TX, bytes);
  }

  @Override
  protected Metrics nextMetrics(long start, long end) {
    return new MetricsImpl(start, end);
  }
}
