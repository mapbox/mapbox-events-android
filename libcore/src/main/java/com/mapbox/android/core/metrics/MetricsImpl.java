package com.mapbox.android.core.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of the metric.
 */
public class MetricsImpl implements Metrics {
  private final long start;
  private final long end;
  private final AtomicLong value;

  MetricsImpl(long start, long end, long initialValue) {
    this.start = start;
    this.end = end;
    this.value = new AtomicLong(initialValue);
  }

  public MetricsImpl(long start, long end) {
    this(start, end, 0L);
  }

  @Override
  public void add(long delta) {
    value.addAndGet(delta);
  }

  @Override
  public long getValue() {
    return value.get();
  }

  @Override
  public long getStart() {
    return start;
  }

  @Override
  public long getEnd() {
    return end;
  }
}
