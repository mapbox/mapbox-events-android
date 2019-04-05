package com.mapbox.android.telemetry.metrics;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCompositeMetrics {
  private final Map<String, Deque<Metrics>> metricsMap = new ConcurrentHashMap<>();
  private final long maxLength;

  public AbstractCompositeMetrics(long maxLength) {
    this.maxLength = maxLength;
  }

  protected abstract Metrics nextMetrics(long start, long end);

  public void add(String name, long delta) {
    long now = SystemClock.uptimeMillis();
    Deque<Metrics> metrics = getOrCreateMetrics(name.trim());

    Metrics last;
    synchronized (this) {
      if (now >= metrics.getLast().getEnd()) {
        metrics.add(nextMetrics(now, now + maxLength));
      }
      last = metrics.getLast();
    }
    last.add(delta);
  }

  @Nullable
  public Metrics getMetrics(@NonNull String name) {
    Deque<Metrics> metrics = metricsMap.get(name.trim());
    return metrics != null && !metrics.isEmpty() ? metrics.pop() : null;
  }

  @NonNull
  private synchronized Deque<Metrics> getOrCreateMetrics(@NonNull String name) {
    Deque<Metrics> metrics;
    if ((metrics = metricsMap.get(name)) == null) {
      metrics = new ArrayDeque<>();
      long now = SystemClock.uptimeMillis();
      metrics.add(nextMetrics(now, now + maxLength));
      metricsMap.put(name, metrics);
    }
    return metrics;
  }
}
