package com.mapbox.android.core.metrics;

import android.os.SystemClock;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsImplTest {

  @Test
  public void wrongSpan() {
    long startTime = SystemClock.elapsedRealtime();
    long endTime = startTime + TimeUnit.HOURS.toMillis(24);
    Metrics metrics = new MetricsImpl(endTime, startTime);
    assertThat(metrics.getStart()).isEqualTo(startTime);
    assertThat(metrics.getEnd()).isEqualTo(endTime);
  }

  @Test
  public void add() {
    Metrics metrics = getMetrics();
    metrics.add(100L);
    assertThat(metrics.getValue()).isEqualTo(100L);
  }

  @Test
  public void subtract() {
    Metrics metrics = getMetrics();
    metrics.add(-100L);
    assertThat(metrics.getValue()).isEqualTo(-100L);
  }

  @Test
  public void getValue() {
    Metrics metrics = getMetrics();
    assertThat(metrics.getValue()).isEqualTo(0L);
  }

  @Test
  public void getStart() {
    long startTime = SystemClock.elapsedRealtime();
    Metrics metrics = new MetricsImpl(startTime, startTime + TimeUnit.HOURS.toMillis(24));
    assertThat(metrics.getStart()).isEqualTo(startTime);
  }

  @Test
  public void getEnd() {
    long endTime = SystemClock.elapsedRealtime();
    Metrics metrics = new MetricsImpl(0, endTime);
    assertThat(metrics.getStart()).isEqualTo(endTime);
  }

  private static Metrics getMetrics() {
    long startTime = SystemClock.elapsedRealtime();
    return new MetricsImpl(startTime, startTime + TimeUnit.HOURS.toMillis(24));
  }
}