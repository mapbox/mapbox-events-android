package com.mapbox.android.telemetry.metrics;

/**
 * Metrics object that maintains counter over a time span
 */
public interface Metrics {
  void add(long delta);
  long getValue();
  long getStart();
  long getEnd();
}