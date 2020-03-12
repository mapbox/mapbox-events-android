package com.mapbox.android.telemetry;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

public class TestReformedUserAgent {

  private static final String CORE_PACKAGE = "com.mapbox.android.core";
  private static final String TELEMETRY_PACKAGE = "com.mapbox.android.telemetry";

  @Test
  public void testReformedUserAgent() {
    Context context = InstrumentationRegistry.getContext();
    String reformedUserAgent = TelemetryUtils.createReformedFullUserAgent(context);
    Assert.assertTrue(reformedUserAgent.contains(context.getPackageName()));
    Assert.assertTrue(reformedUserAgent.contains(CORE_PACKAGE));
    Assert.assertTrue(reformedUserAgent.contains(TELEMETRY_PACKAGE));
    Assert.assertFalse(reformedUserAgent.contains("null"));
  }

  @Test(expected = NullPointerException.class)
  public void testReformedUserAgentForNullContext() {
    TelemetryUtils.createReformedFullUserAgent(null);
  }
}
