package com.mapbox.android.core.crashreporter;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class CrashReportBuilderInstrumentedTest {
  private final String validJson = "{\"locations\":[94043,90210],\"query\":\"Pizza\"}";
  private static final String TELEM_MAPBOX_PACKAGE = "com.mapbox.android.telemetry";
  private static final String TELEM_MAPBOX_VERSION = "4.0.0";

  private CrashReportBuilder builder;

  @Before
  public void setUp() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    builder = CrashReportBuilder.setup(context, TELEM_MAPBOX_PACKAGE,
      TELEM_MAPBOX_VERSION, Collections.<String>emptySet());
  }

  @After
  public void tearDown() {
    builder = null;
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidBodyfromJson() {
    CrashReportBuilder.fromJson("/");
  }

  @Test
  public void validBodyfromJson() {
    CrashReport report = CrashReportBuilder.fromJson(validJson);
    assertEquals(validJson.trim(), report.toJson());
  }

  @Test
  public void buildOffDefaults() {
    try {
      builder.build();
    } catch (Exception npe) {
      fail("Unexpected exception: " + npe.toString());
    }
  }
}
