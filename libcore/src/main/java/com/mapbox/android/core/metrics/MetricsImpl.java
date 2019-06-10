package com.mapbox.android.core.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of the thread safe
 * metric with time span.
 */
public class MetricsImpl implements Metrics {
  private final long start;
  private final long end;
  private final AtomicLong value;

  MetricsImpl(long start, long end, long initialValue) {
    if (start > end) {
      this.start = end;
      this.end = start;
    } else {
      this.start = start;
      this.end = end;
    }
    this.value = new AtomicLong(initialValue);
  }

  /**
   * Intantiate new metric with a span.
   * @param start timestamp
   * @param end timestamp
   */
  public MetricsImpl(long start, long end) {
    this(start, end, 0L);
  }

  /**
   * Increment metric by delta. (thread safe)
   *
   * @param delta value
   */
  @Override
  public void add(long delta) {
    value.addAndGet(delta);
  }

  /**
   * Return metric value. (thread safe)
   *
   * @return metric value
   */
  @Override
  public long getValue() {
    return value.get();
  }

  /**
   * Return span start timestamp.
   *
   * @return timestamp in milliseconds
   */
  @Override
  public long getStart() {
    return start;
  }

  /**
   * Return span end timestamp.
   *
   * @return timestamp in milliseconds
   */
  @Override
  public long getEnd() {
    return end;
  }
}
