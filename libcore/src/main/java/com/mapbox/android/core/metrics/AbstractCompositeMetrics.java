package com.mapbox.android.core.metrics;

import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks stats over rolling time window of the max length.
 * Optimized for write heavy metrics.
 */
public abstract class AbstractCompositeMetrics {
  private final Map<String, Deque<Metrics>> metricsMap = new ConcurrentHashMap<>();
  private final long maxLength;

  /**
   * Create instance of the composite metric.
   *
   * @param maxLength max time window in milliseconds.
   */
  public AbstractCompositeMetrics(long maxLength) {
    this.maxLength = maxLength;
  }

  /**
   * Called by child class when new metrics is needed.
   * Concrete implementation of the metric is delegated to child class.
   *
   * @param start of the time span.
   * @param end of the time span.
   * @return reference to the new metric object.
   */
  protected abstract Metrics nextMetrics(long start, long end);

  /**
   * Adds value to the metric and occasionally creates new metric
   * if the delta is out of the exiting metric span.
   *
   * @param name name of the metric.
   * @param delta value to increment.
   */
  public void add(String name, long delta) {
    long now = SystemClock.uptimeMillis();

    Metrics last;
    synchronized (this) {
      Deque<Metrics> metrics = getOrCreateMetrics(name.trim());
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
    synchronized (this) {
      return metrics != null && !metrics.isEmpty() ? metrics.pop() : null;
    }
  }

  @NonNull
  private Deque<Metrics> getOrCreateMetrics(@NonNull String name) {
    Deque<Metrics> metrics;
    if ((metrics = metricsMap.get(name)) == null) {
      metrics = new ArrayDeque<>();
      metricsMap.put(name, metrics);
    }

    if (metrics.isEmpty()) {
      long now = SystemClock.uptimeMillis();
      metrics.add(nextMetrics(now, now + maxLength));
    }
    return metrics;
  }
}
