package com.mapbox.android.telemetry;


public class SessionInterval {
  private static final String INTERVAL_NOT_IN_THE_RANGE = "The interval passed in must be an an integer in the"
    + " range of 1 to 24 hours.";
  private static final int LOW_INTERVAL_VALUE = 1;
  private static final int HIGH_INTERVAL_VALUE = 24;
  private final int interval;

  public SessionInterval(int interval) {
    check(interval);
    this.interval = interval;
  }

  private void check(int interval) {
    if (interval < LOW_INTERVAL_VALUE || interval > HIGH_INTERVAL_VALUE) {
      throw new IllegalArgumentException(INTERVAL_NOT_IN_THE_RANGE);
    }
  }

  int obtainInterval() {
    return interval;
  }
}
