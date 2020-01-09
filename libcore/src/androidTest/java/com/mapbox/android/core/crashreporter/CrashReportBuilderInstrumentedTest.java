package com.mapbox.android.core.crashreporter;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_CRASH_REPORTER_PREFERENCES;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CrashReportBuilderInstrumentedTest {
  private final String validJson = "{\"query\":\"Pizza\",\"locations\":[94043,90210]}";
  private static final String TELEM_MAPBOX_PACKAGE = "com.mapbox.android.telemetry";
  private static final String TELEM_MAPBOX_VERSION = "4.0.0";

  private MapboxUncaughtExceptionHanlder exceptionHanlder;
  private CrashReportBuilder builder;

  @Before
  public void setUp() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    exceptionHanlder = new MapboxUncaughtExceptionHanlder(context,
        context.getSharedPreferences(MAPBOX_CRASH_REPORTER_PREFERENCES, Context.MODE_PRIVATE),
        TELEM_MAPBOX_PACKAGE, TELEM_MAPBOX_VERSION, Thread.getDefaultUncaughtExceptionHandler());
    builder = CrashReportBuilder.setup(context, TELEM_MAPBOX_PACKAGE, TELEM_MAPBOX_VERSION);
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
  public void checkMandatoryAttributes() {
    List<Throwable> causalChain = exceptionHanlder.getCausalChain(createMapboxThrowable());
    CrashReport report = builder
      .addExceptionThread(Thread.currentThread())
      .addCausalChain(causalChain)
      .build();

    assertEquals("mobile.crash", report.getString("event"));
    assertFalse(report.getString("created").isEmpty());
  }

  @Test
  public void buildOffDefaults() {
    try {
      builder.build();
    } catch (Exception npe) {
      fail("Unexpected exception: " + npe.toString());
    }
  }

  @Test
  public void getStackTrace() {
    List<Throwable> causalChain = exceptionHanlder.getCausalChain(createMapboxThrowable());
    String stackTrace = builder.getStackTrace(causalChain);
    assertEquals("com.mapbox.android.telemetry.A.foo(com.mapbox.android.telemetry.A.java:0)\n", stackTrace);
  }

  @Test
  public void checkStackTraceHash() {
    List<Throwable> causalChain = exceptionHanlder.getCausalChain(createMapboxThrowable());
    String hash = CrashReportBuilder.getStackTraceHash(causalChain);
    assertEquals("34cbd8fe", hash);
  }

  @Test
  public void checkFullStackTraceHash() {
    exceptionHanlder.setExceptionChainDepth(1);
    List<Throwable> causalChain = exceptionHanlder.getCausalChain(createMapboxThrowable());
    String hash = CrashReportBuilder.getStackTraceHash(causalChain);
    assertEquals("941ea8f5", hash);
  }

  private static Throwable createMapboxThrowable() {
    Throwable highLevelThrowable = createThrowable("HighLevelThrowable", "A", "B");
    Throwable midLevelThrowable = createThrowable("MidLevelThrowable", "A", "B", "C", "D");
    midLevelThrowable.initCause(createThrowable("LowLevelThrowable",
      TELEM_MAPBOX_PACKAGE + ".A", "F", "G"));
    highLevelThrowable.initCause(midLevelThrowable);
    return highLevelThrowable;
  }

  private static Throwable createThrowable(String message, String... stackTraceElements) {
    StackTraceElement[] array = new StackTraceElement[stackTraceElements.length];
    for (int i = 0; i < stackTraceElements.length; i++) {
      String s = stackTraceElements[i];
      array[stackTraceElements.length - 1 - i]
        = new StackTraceElement(s, "foo", s + ".java", i);
    }
    Throwable result = new Throwable(message);
    result.setStackTrace(array);
    return result;
  }
}
