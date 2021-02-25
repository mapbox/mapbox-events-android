package com.mapbox.android.core.crashreporter;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class CrashReportFactoryInstrumentedTest {
  private static final String TELEM_MAPBOX_PACKAGE = "com.mapbox.android.telemetry";
  private static final String TELEM_MAPBOX_VERSION = "4.0.0";

  private static final String JAVA_UTIL_PACKAGE = "java.util";
  private static final Set<String> ALLOWED_PACKAGE_PREFIXES = new HashSet<>();
  private static final Map<String, String> TEST_CUSTOM_DATA = new HashMap<>();

  static  {
    ALLOWED_PACKAGE_PREFIXES.add(JAVA_UTIL_PACKAGE);
    TEST_CUSTOM_DATA.put("testKey", "testValue");
  }

  private CrashReportFactory factory;

  @Before
  public void setUp() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    factory = new CrashReportFactory(context, TELEM_MAPBOX_PACKAGE, TELEM_MAPBOX_VERSION,
      ALLOWED_PACKAGE_PREFIXES);
  }

  @After
  public void tearDown() {
    factory = null;
  }

  @Test
  public void checkMandatoryAttributesForCrash() {
    CrashReport report = factory.createReportForCrash(Thread.currentThread(), createMapboxThrowable());

    assertNotNull(report);
    assertEquals("mobile.crash", report.getString("event"));
    assertFalse(report.getString("created").isEmpty());
  }

  @Test
  public void checkMandatoryAttributesForNonFatal() {
    CrashReport report = factory.createReportForNonFatal(createMapboxThrowable());

    assertNotNull(report);
    assertEquals("mobile.crash", report.getString("event"));
    assertFalse(report.getString("created").isEmpty());
  }

  @Test
  public void checkMandatoryAttributesForCrashWithEmptyCustomData() {
    CrashReport report = factory.createReportForCrash(Thread.currentThread(),
      createMapboxThrowable(), Collections.<String, String>emptyMap());

    assertNotNull(report);
    assertEquals("mobile.crash", report.getString("event"));
    assertNull(report.getJsonArray("customData"));
    assertFalse(report.getString("created").isEmpty());
  }

  @Test
  public void checkMandatoryAttributesForNonFatalWithEmptyCustomData() {
    CrashReport report = factory.createReportForNonFatal(createMapboxThrowable(),
      Collections.<String, String>emptyMap());

    assertNotNull(report);
    assertEquals("mobile.crash", report.getString("event"));
    assertNull(report.getJsonArray("customData"));
    assertFalse(report.getString("created").isEmpty());
  }

  @Test
  public void checkMandatoryAttributesForCrashWithCustomData() {
    CrashReport report = factory.createReportForCrash(Thread.currentThread(),
      createMapboxThrowable(), TEST_CUSTOM_DATA);

    assertNotNull(report);
    assertEquals("mobile.crash", report.getString("event"));
    assertEquals("[{\"name\":\"testKey\",\"value\":\"testValue\"}]", report.getJsonArray("customData").toString());
    assertFalse(report.getString("created").isEmpty());
  }

  @Test
  public void checkMandatoryAttributesForNonFatalWithCustomData() {
    CrashReport report = factory.createReportForNonFatal(createMapboxThrowable(), TEST_CUSTOM_DATA);

    assertNotNull(report);
    assertEquals("mobile.crash", report.getString("event"));
    assertEquals("[{\"name\":\"testKey\",\"value\":\"testValue\"}]", report.getJsonArray("customData").toString());
    assertFalse(report.getString("created").isEmpty());
  }

  @Test
  public void getStackTrace() {
    factory.setCrashChainDepth(2);
    CrashReport report = factory.createReportForCrash(Thread.currentThread(), createMapboxThrowable());

    assertNotNull(report);
    assertEquals("***\n"
        + "*\n"
        + "*\n"
        + "*\n"
        + "*\n"
        + "***\n"
        + "*\n"
        + "*\n"
        + "com.mapbox.android.telemetry.A.foo(com.mapbox.android.telemetry.A.java:0)\n",
      report.getString("stackTrace"));
  }


  @Test
  public void checkStackTraceHashForCrash() {
    factory.setCrashChainDepth(2);
    CrashReport report = factory.createReportForCrash(Thread.currentThread(), createMapboxThrowable());

    assertNotNull(report);
    assertEquals("34cbd8fe", report.getString("stackTraceHash"));
  }

  @Test
  public void checkStackTraceHashForNonFatal() {
    CrashReport report = factory.createReportForNonFatal(createMapboxThrowable());

    assertNotNull(report);
    assertEquals("941ea8f5", report.getString("stackTraceHash"));
  }

  @Test
  public void checkStackTraceWithAllowedPackagesCrash() {
    factory.setCrashChainDepth(2);
    Throwable highLevelThrowable = createThrowable("HighLevelThrowable",
      JAVA_UTIL_PACKAGE + ".A", JAVA_UTIL_PACKAGE + ".B");
    Throwable midLevelThrowable = createThrowable("MidLevelThrowable",
      "A", "B", "C");
    midLevelThrowable.initCause(createThrowable("LowLevelThrowable",
      TELEM_MAPBOX_PACKAGE + ".A", "D", "E"));
    highLevelThrowable.initCause(midLevelThrowable);

    CrashReport report = factory.createReportForCrash(Thread.currentThread(), highLevelThrowable);

    assertNotNull(report);
    assertEquals("***\n"
        + "*\n"
        + "*\n"
        + "*\n"
        + "***\n"
        + "*\n"
        + "*\n"
        + "com.mapbox.android.telemetry.A.foo(com.mapbox.android.telemetry.A.java:0)\n",
      report.getString("stackTrace"));
  }

  @Test
  public void checkStackTraceWithAllowedPackagesNonFatal() {
    Throwable highLevelThrowable = createThrowable("HighLevelThrowable",
      JAVA_UTIL_PACKAGE + ".A", JAVA_UTIL_PACKAGE + ".B");
    Throwable midLevelThrowable = createThrowable("MidLevelThrowable",
      "A", "B", "C");
    midLevelThrowable.initCause(createThrowable("LowLevelThrowable",
      TELEM_MAPBOX_PACKAGE + ".A", "D", "E"));
    highLevelThrowable.initCause(midLevelThrowable);

    CrashReport report = factory.createReportForNonFatal(highLevelThrowable);

    assertNotNull(report);
    assertEquals("HighLevelThrowable\n"
        + "java.util.B.foo(java.util.B.java:1)\n"
        + "java.util.A.foo(java.util.A.java:0)\n"
        + "***\n"
        + "*\n"
        + "*\n"
        + "*\n"
        + "***\n"
        + "*\n"
        + "*\n"
        + "com.mapbox.android.telemetry.A.foo(com.mapbox.android.telemetry.A.java:0)\n",
      report.getString("stackTrace"));
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
