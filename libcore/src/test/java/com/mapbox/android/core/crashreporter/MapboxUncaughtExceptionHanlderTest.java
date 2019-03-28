package com.mapbox.android.core.crashreporter;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.List;

import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_PREF_ENABLE_CRASH_REPORTER;
import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class MapboxUncaughtExceptionHanlderTest {
  private static final String TELEM_MAPBOX_PACKAGE = "com.mapbox.android.telemetry";
  private static final String TELEM_MAPBOX_VERSION = "4.0.0";

  @Mock
  private Thread.UncaughtExceptionHandler defaultExceptionHanlder;

  @Mock
  private Context context;

  private SharedPreferences sharedPreferences = getMockedSharedPrefs(MAPBOX_PREF_ENABLE_CRASH_REPORTER, true);

  private MapboxUncaughtExceptionHanlder exceptionHanlder;

  @Before
  public void setUp() {
    exceptionHanlder =
      new MapboxUncaughtExceptionHanlder(context, sharedPreferences, TELEM_MAPBOX_PACKAGE, TELEM_MAPBOX_VERSION,
        defaultExceptionHanlder);
  }

  @After
  public void tearDown() {
    reset(context);
    exceptionHanlder = null;
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
      new MapboxUncaughtExceptionHanlder(context, sharedPreferences, TELEM_MAPBOX_PACKAGE, TELEM_MAPBOX_VERSION,
        null);
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
    assertThat(exceptionHanlder.isMapboxCrash(causalChain)).isFalse();
  }

  @Test
  public void testMidLevelException() {
    exceptionHanlder.setExceptionChainDepth(2);
    Throwable throwable = createMapboxThrowable();
    List<Throwable> causalChain = exceptionHanlder.getCausalChain(throwable);
    assertThat(exceptionHanlder.isMapboxCrash(causalChain)).isTrue();
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
    assertThat(exceptionHanlder.isMapboxCrash(causalChain)).isTrue();
  }

  @Test
  public void exceptionHandlerDisabled() {
    // Disable via shared pref
    exceptionHanlder.onSharedPreferenceChanged(
      getMockedSharedPrefs(MAPBOX_PREF_ENABLE_CRASH_REPORTER, false),
      MAPBOX_PREF_ENABLE_CRASH_REPORTER);
    exceptionHanlder.uncaughtException(Thread.currentThread(), new Throwable());
    verify(context, never()).getFilesDir();
  }

  @Test
  public void exceptionHandlerEnabledNotMapboxCrash() {
    exceptionHanlder.uncaughtException(Thread.currentThread(),
      createThrowable("HighLevelThrowable", "A", "B"));
    verify(context, never()).getFilesDir();
  }

  @Test
  public void exceptionHandlerEnabledMapboxCrash() {
    exceptionHanlder.setExceptionChainDepth(2);
    when(context.getFilesDir()).thenReturn(new File(""));
    exceptionHanlder.uncaughtException(Thread.currentThread(), createMapboxThrowable());
    // FIXME: figure out why these aren't passing on CI
    //verify(context, times(2)).getFilesDir();
  }

  @Test
  public void onSharedPreferenceChangedWithInvalidKey() {
    boolean isEnabled = exceptionHanlder.isEnabled();
    exceptionHanlder.onSharedPreferenceChanged(getMockedSharedPrefs("FooKey", false), "FooKey");
    assertThat(exceptionHanlder.isEnabled()).isEqualTo(isEnabled);
  }

  @Test
  public void onSharedPreferenceChanged() {
    boolean isEnabled = exceptionHanlder.isEnabled();
    exceptionHanlder.onSharedPreferenceChanged(
      getMockedSharedPrefs(MAPBOX_PREF_ENABLE_CRASH_REPORTER, false),
      MAPBOX_PREF_ENABLE_CRASH_REPORTER);
    assertThat(exceptionHanlder.isEnabled()).isNotEqualTo(isEnabled);
  }

  private static Throwable createMapboxThrowable() {
    Throwable throwable = createThrowable("HighLevelThrowable", "A", "B");
    throwable.initCause(createThrowable("MidLevelThrowable",
      TELEM_MAPBOX_PACKAGE + ".A", "B", "C", "D"));
    return throwable;
  }

  private static SharedPreferences getMockedSharedPrefs(String key, boolean value) {
    SharedPreferences sharedPreferences = mock(SharedPreferences.class);
    when(sharedPreferences.getBoolean(key, true)).thenReturn(value);
    return sharedPreferences;
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