package com.mapbox.android.telemetry.metrics;

import androidx.annotation.IntRange;
import androidx.annotation.VisibleForTesting;
import com.mapbox.android.core.metrics.AbstractCompositeMetrics;
import com.mapbox.android.core.metrics.Metrics;
import com.mapbox.android.core.metrics.MetricsImpl;

import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_VPN;

public class TelemetryMetrics extends AbstractCompositeMetrics {
  public static final String EVENTS_TOTAL = "eventCountTotal";
  public static final String EVENTS_FAILED = "eventCountFailed";

  @VisibleForTesting
  static final String MOBILE_BYTES_TX = "cellDataSent";
  static final String WIFI_BYTES_TX = "wifiDataSent";
  static final String MOBILE_BYTES_RX = "cellDataReceived";
  static final String WIFI_BYTES_RX = "wifiDataReceived";

  public TelemetryMetrics(long maxLength) {
    super(maxLength);
  }

  public void addRxBytesForType(@IntRange(from = TYPE_MOBILE, to = TYPE_VPN) int networkType, long bytes) {
    if (isValidNetworkType(networkType)) {
      add(networkType == TYPE_WIFI ? WIFI_BYTES_RX : MOBILE_BYTES_RX, bytes);
    }
  }

  public void addTxBytesForType(@IntRange(from = TYPE_MOBILE, to = TYPE_VPN) int networkType, long bytes) {
    if (isValidNetworkType(networkType)) {
      add(networkType == TYPE_WIFI ? WIFI_BYTES_TX : MOBILE_BYTES_TX, bytes);
    }
  }

  @Override
  protected Metrics nextMetrics(long start, long end) {
    return new MetricsImpl(start, end);
  }

  private static boolean isValidNetworkType(int type) {
    return type >= TYPE_MOBILE && type <= TYPE_VPN;
  }
}
