package com.mapbox.android.core.crashreporter;

import android.content.Context;

import com.mapbox.android.core.utils.NoStackTraceException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static com.mapbox.android.core.utils.ErrorTestUtils.createMapboxThrowable;
import static com.mapbox.android.core.utils.ErrorTestUtils.createThrowable;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public class CrashReportFactoryTest {
  private static final String TELEM_MAPBOX_PACKAGE = "com.mapbox.android.telemetry";
  private static final String TELEM_MAPBOX_VERSION = "4.0.0";

  @Mock
  private Context context;

  private CrashReportFactory crashReportFactory;

  @Before
  public void setUp() {
    crashReportFactory = new CrashReportFactory(context, TELEM_MAPBOX_PACKAGE,
      TELEM_MAPBOX_VERSION, Collections.<String>emptySet());
  }

  @After
  public void tearDown() {
    reset(context);
    crashReportFactory = null;
  }

  @Test
  public void testCrashNullStackTrace() {
    crashReportFactory.setCrashChainDepth(1);
    try {
      crashReportFactory.createReportForCrash(Thread.currentThread(), new NoStackTraceException());
    } catch (Throwable throwable) {
      fail("Unexpected assertion due to null stacktrace: " + throwable.toString());
    }
  }

  @Test
  public void testNonFatalNullStackTrace() {
    try {
      crashReportFactory.createReportForNonFatal(new NoStackTraceException());
    } catch (Throwable throwable) {
      fail("Unexpected assertion due to null stacktrace: " + throwable.toString());
    }
  }

  @Test
  public void testHighLevelCrash() {
    crashReportFactory.setCrashChainDepth(2);
    Throwable throwable = createThrowable("HighLevelThrowable", "A", "B");
    CrashReport crashReport = crashReportFactory.createReportForCrash(Thread.currentThread(), throwable);
    assertThat(crashReport).isNull();
  }

  @Test
  public void testHighLevelNonFatal() {
    Throwable throwable = createThrowable("HighLevelThrowable", "A", "B");
    CrashReport crashReport = crashReportFactory.createReportForNonFatal(throwable);
    assertThat(crashReport).isNull();
  }

  @Test
  public void testMidLevelCrash() {
    crashReportFactory.setCrashChainDepth(2);
    Throwable throwable = createMapboxThrowable(TELEM_MAPBOX_PACKAGE);
    CrashReport crashReport = crashReportFactory.createReportForCrash(null, throwable);
    assertThat(crashReport).isNotNull();
  }

  @Test
  public void testMidLevelNonFatal() {
    Throwable throwable = createMapboxThrowable(TELEM_MAPBOX_PACKAGE);
    CrashReport crashReport = crashReportFactory.createReportForNonFatal(throwable);
    assertThat(crashReport).isNotNull();
  }

  @Test
  public void testLowLevelCrash() {
    crashReportFactory.setCrashChainDepth(2);
    Throwable highLevelThrowable = createThrowable("HighLevelThrowable", "A", "B");
    Throwable midLevelThrowable = createThrowable("MidLevelThrowable", "A", "B", "C", "D");
    midLevelThrowable.initCause(createThrowable("LowLevelThrowable",
      TELEM_MAPBOX_PACKAGE + ".A", "F", "G"));
    highLevelThrowable.initCause(midLevelThrowable);
    CrashReport crashReport = crashReportFactory.createReportForCrash(null, highLevelThrowable);
    assertThat(crashReport).isNotNull();
  }

  @Test
  public void testLowLevelNonFatal() {
    Throwable highLevelThrowable = createThrowable("HighLevelThrowable", "A", "B");
    Throwable midLevelThrowable = createThrowable("MidLevelThrowable", "A", "B", "C", "D");
    midLevelThrowable.initCause(createThrowable("LowLevelThrowable",
      TELEM_MAPBOX_PACKAGE + ".A", "F", "G"));
    highLevelThrowable.initCause(midLevelThrowable);
    CrashReport crashReport = crashReportFactory.createReportForNonFatal(highLevelThrowable);
    assertThat(crashReport).isNotNull();
  }
}
