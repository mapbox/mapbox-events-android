package com.mapbox.android.core.crashreporter;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MapboxUncaughtExceptionHanlderTest {
  private static final String TELEM_MAPBOX_PACKAGE = "com.mapbox.android.telemetry";

  @Mock
  private Thread.UncaughtExceptionHandler defaultExceptionHanlder;

  @Mock
  private Context context;

  @Mock
  private SharedPreferences sharedPreferences;

  private MapboxUncaughtExceptionHanlder exceptionHanlder;

  @Before
  public void setUp() {
    exceptionHanlder =
      new MapboxUncaughtExceptionHanlder(context, sharedPreferences, TELEM_MAPBOX_PACKAGE, defaultExceptionHanlder);
  }

  @After
  public void tearDown() {
    reset(context);
    exceptionHanlder = null;
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidPackageName() {
    new MapboxUncaughtExceptionHanlder(context, sharedPreferences, "", defaultExceptionHanlder);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullPackageName() {
    new MapboxUncaughtExceptionHanlder(context, sharedPreferences, null, defaultExceptionHanlder);
  }

  @Test
  public void testDefaultExceptionHanlder() {
    exceptionHanlder.uncaughtException(Thread.currentThread(), new Throwable());
    verify(defaultExceptionHanlder, times(1))
      .uncaughtException(any(Thread.class), any(Throwable.class));
  }

  @Test
  public void testNullDefaultExceptionHanlder() {
    exceptionHanlder =
      new MapboxUncaughtExceptionHanlder(context, sharedPreferences, TELEM_MAPBOX_PACKAGE, null);
    try {
      exceptionHanlder.uncaughtException(Thread.currentThread(), new Throwable());
    } catch (Throwable throwable) {
      fail("Unexpected assertion due to null default handler: " + throwable.toString());
    }
  }

  @Test
  public void testNullStackTrace() {
    exceptionHanlder.setExceptionChainDepth(1);
    try {
      exceptionHanlder.uncaughtException(Thread.currentThread(), new NoStackTraceException());
    } catch (Throwable throwable) {
      fail("Unexpected assertion due to null stacktrace: " + throwable.toString());
    }
  }

  @Test
  public void testHighLevelException() {
    exceptionHanlder.setExceptionChainDepth(2);
    Throwable throwable = createThrowable("HighLevelThrowable", "A", "B");

    List<Throwable> causalChain = exceptionHanlder.getCausalChain(throwable);
    assertFalse(exceptionHanlder.isMapboxCrash(causalChain));
  }

  @Test
  public void testMidLevelException() {
    exceptionHanlder.setExceptionChainDepth(2);
    Throwable throwable = createThrowable("HighLevelThrowable", "A", "B");
    throwable.initCause(createThrowable("MidLevelThrowable",
      TELEM_MAPBOX_PACKAGE + ".A", "B", "C", "D"));

    List<Throwable> causalChain = exceptionHanlder.getCausalChain(throwable);
    assertTrue(exceptionHanlder.isMapboxCrash(causalChain));
  }

  @Test
  public void testLowLevelException() {
    exceptionHanlder.setExceptionChainDepth(2);
    Throwable highLevelThrowable = createThrowable("HighLevelThrowable", "A", "B");
    Throwable midLevelThrowable = createThrowable("MidLevelThrowable", "A", "B", "C", "D");
    midLevelThrowable.initCause(createThrowable("LowLevelThrowable",
      TELEM_MAPBOX_PACKAGE + ".A", "F", "G"));
    highLevelThrowable.initCause(midLevelThrowable);

    List<Throwable> causalChain = exceptionHanlder.getCausalChain(highLevelThrowable);
    assertTrue(exceptionHanlder.isMapboxCrash(causalChain));
  }

  @Test
  public void uncaughtException() {
    /// Handle exception

    /// Test file is written
  }

  @Test
  public void testFileContent() {

  }

  @Test
  public void onSharedPreferenceChanged() {
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

  private static class NoStackTraceException extends Exception {
    @Override
    public synchronized Throwable fillInStackTrace() {
      return null;
    }
  }
}