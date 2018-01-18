package com.mapbox.android.telemetry;

import org.junit.Test;

public class SessionIntervalTest {

  @Test(expected = IllegalArgumentException.class)
  public void checksIntervalNotInRangeLow() throws Exception {
    SessionInterval sessionIntervalUnderOne = new SessionInterval(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksIntervalNotInRangeHigh() throws Exception {
    SessionInterval sessionIntervalOverTwentyFour = new SessionInterval(25);
  }
}