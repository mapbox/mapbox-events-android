package com.mapbox.android.core.crashreporter;

import androidx.test.platform.app.InstrumentationRegistry;
import com.mapbox.android.core.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class FileUtilsInstrumentedTest {
  private static final String TELEM_MAPBOX_PACKAGE = "com.mapbox.android.telemetry";
  private final String validJson = "{\"query\":\"Pizza\",\"locations\":[94043,90210]}";
  private static final String filename = MapboxUncaughtExceptionHanlder
    .getReportFileName(TELEM_MAPBOX_PACKAGE, "12345678");

  private File directory;
  private File file;

  @Before
  public void setUp() {
    directory = FileUtils.getFile(InstrumentationRegistry.getInstrumentation().getTargetContext(),
        TELEM_MAPBOX_PACKAGE);
    if (!directory.exists()) {
      directory.mkdir();
    }

    for (File file: directory.listFiles()) {
      file.delete();
    }

    file = FileUtils.getFile(InstrumentationRegistry.getInstrumentation().getTargetContext(), filename);
  }

  @Test
  public void writeToFile() {
    try {
      FileUtils.writeToFile(file, validJson);
    } catch (Exception exception) {
      fail("Unexpected IOException thrown: " + exception.toString());
    }
    assertTrue(file.exists());
    assertTrue(file.length() > 0);
  }

  @Test
  public void readFromFile() {
    try {
      FileUtils.writeToFile(file, validJson);
      String json = FileUtils.readFromFile(file);
      assertEquals(json, validJson);
    } catch (Exception exception) {
      fail("Unexpected IOException thrown: " + exception.toString());
    }
  }
}