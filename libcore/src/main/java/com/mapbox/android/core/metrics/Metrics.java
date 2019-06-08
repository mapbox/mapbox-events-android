package com.mapbox.android.core.metrics;

/**
 * Metrics object counter over a time span
 */
public interface Metrics {

  /**
   * Increment metric
   *
   * @param delta value
   */
  void add(long delta);

  /**
   * Return current metric value
   *
   * @return current state of the metric
   */
  long getValue();

  /**
   * Return start of the time span [start, end]
   *
   * @return timestamp in milliseconds.
   */
  long getStart();

  /**
   * Return end of the time span [start, end]
   *
   * @return timestamp in milliseconds.
   */
  long getEnd();
}