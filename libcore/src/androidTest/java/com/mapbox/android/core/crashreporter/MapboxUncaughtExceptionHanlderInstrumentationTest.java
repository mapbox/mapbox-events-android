package com.mapbox.android.core.crashreporter;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.mapbox.android.core.FileUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_CRASH_REPORTER_PREFERENCES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MapboxUncaughtExceptionHanlderInstrumentationTest {
  private static final String TELEM_MAPBOX_PACKAGE = "com.mapbox.android.telemetry";
  private static final String TELEM_MAPBOX_VERSION = "4.0.0";
  private static final String CRASH_FILENAME_FORMAT = "%s/%s.crash";
  private static final String crashEvent =
    "{\"event\":\"mobile.crash\",\"created\":\"2019-02-21T21:58:43.000Z\",\"stackTraceHash\":\"%s\"}";

  private MapboxUncaughtExceptionHanlder exceptionHanlder;
  private File directory;
  private Context context;

  @Before
  public void setUp() {
    context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    exceptionHanlder = new MapboxUncaughtExceptionHanlder(context,
      context.getSharedPreferences(MAPBOX_CRASH_REPORTER_PREFERENCES, Context.MODE_PRIVATE),
      TELEM_MAPBOX_PACKAGE, TELEM_MAPBOX_VERSION, null);

    directory = FileUtils.getFile(context, TELEM_MAPBOX_PACKAGE);
    if (!directory.exists()) {
      directory.mkdir();
    }

    for (File file: directory.listFiles()) {
      file.delete();
    }
  }

  @Test
  public void testInstall() {
    MapboxUncaughtExceptionHanlder.install(InstrumentationRegistry.getInstrumentation().getTargetContext(),
        TELEM_MAPBOX_PACKAGE,
        TELEM_MAPBOX_VERSION);
    //TODO: verify successful install
  }

  @Test
  public void testReportCap() throws IOException {
    writeToDisk(10);
    exceptionHanlder.setExceptionChainDepth(1);
    exceptionHanlder.uncaughtException(Thread.currentThread(), createMapboxThrowable());

    File[] files = directory.listFiles();
    assertNotNull(files);
    assertEquals(2, files.length);
  }

  @Test
  public void ensureDirectoryWritableHasNoEffect() throws IOException {
    writeToDisk(3);
    MapboxUncaughtExceptionHanlder.ensureDirectoryWritable(context, TELEM_MAPBOX_PACKAGE);
    assertEquals(3, directory.listFiles().length);
  }

  @Test
  public void ensureDirectoryWritableCleansUp() throws IOException {
    writeToDisk(10);
    MapboxUncaughtExceptionHanlder.ensureDirectoryWritable(context, TELEM_MAPBOX_PACKAGE);
    assertEquals(1, directory.listFiles().length);
  }

  private void writeToDisk(int numFiles) throws IOException {
    for (int i = 0; i < numFiles; i++) {
      File file = FileUtils.getFile(context,
        String.format(CRASH_FILENAME_FORMAT, TELEM_MAPBOX_PACKAGE, String.format("crash%d", i)));
      FileUtils.writeToFile(file, String.format(crashEvent, UUID.randomUUID().toString()));
    }
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