package com.mapbox.android.telemetry.metrics;

import android.net.ConnectivityManager;
import com.mapbox.android.core.metrics.Metrics;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class TelemetryMetricsTest {
  private TelemetryMetrics telemetryMetrics;

  @Before
  public void setUp() {
    telemetryMetrics = new TelemetryMetrics(TimeUnit.MINUTES.toMillis(10));
  }

  @Test
  public void addRxBytesForMobileType() {
    telemetryMetrics.addRxBytesForType(ConnectivityManager.TYPE_MOBILE, 10L);
    Metrics metrics = telemetryMetrics.getMetrics(TelemetryMetrics.MOBILE_BYTES_RX);
    assertThat(metrics.getValue()).isEqualTo(10L);
  }

  @Test
  public void addRxBytesForWifiType() {
    telemetryMetrics.addRxBytesForType(ConnectivityManager.TYPE_WIFI, 10L);
    Metrics metrics = telemetryMetrics.getMetrics(TelemetryMetrics.WIFI_BYTES_RX);
    assertThat(metrics.getValue()).isEqualTo(10L);
  }

  @Test
  public void addTxBytesForWifiType() {
    telemetryMetrics.addTxBytesForType(ConnectivityManager.TYPE_WIFI, 10L);
    Metrics metrics = telemetryMetrics.getMetrics(TelemetryMetrics.WIFI_BYTES_TX);
    assertThat(metrics.getValue()).isEqualTo(10L);
  }

  @Test
  public void addTxBytesForMobileType() {
    telemetryMetrics.addTxBytesForType(ConnectivityManager.TYPE_MOBILE, 10L);
    Metrics metrics = telemetryMetrics.getMetrics(TelemetryMetrics.MOBILE_BYTES_TX);
    assertThat(metrics.getValue()).isEqualTo(10L);
  }

  @Test
  public void addBytesForUnsupportedType() {
    telemetryMetrics.addTxBytesForType(1000, 10L);
    telemetryMetrics.addRxBytesForType(1000, 10L);
    assertThat(telemetryMetrics.getMetrics(TelemetryMetrics.MOBILE_BYTES_TX)).isNull();
    assertThat(telemetryMetrics.getMetrics(TelemetryMetrics.WIFI_BYTES_TX)).isNull();
    assertThat(telemetryMetrics.getMetrics(TelemetryMetrics.MOBILE_BYTES_RX)).isNull();
    assertThat(telemetryMetrics.getMetrics(TelemetryMetrics.WIFI_BYTES_RX)).isNull();
  }
}