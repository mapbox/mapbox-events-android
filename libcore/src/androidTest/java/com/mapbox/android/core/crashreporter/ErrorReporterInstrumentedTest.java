package com.mapbox.android.core.crashreporter;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.mapbox.android.core.FileUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

import static junit.framework.TestCase.assertTrue;

public class ErrorReporterInstrumentedTest {
  private static final String TELEM_MAPBOX_PACKAGE = "com.mapbox.android.telemetry";
  private static final String ERROR_STRING = "Some error occurred.";
  private static final String ERROR_FILE_EXTENSION = ".crash";

  private File directory;
  private Context context;

  @Before
  public void setUp() {
    context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    directory = FileUtils.getFile(context, TELEM_MAPBOX_PACKAGE);
    if (!directory.exists()) {
      directory.mkdir();
    }

    for (File file : directory.listFiles()) {
      file.delete();
    }
  }

  @Test
  public void reportError() throws FileNotFoundException {
    ErrorReporter.reportError(context, TELEM_MAPBOX_PACKAGE, new Throwable(ERROR_STRING));

    File[] files = directory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().endsWith(ERROR_FILE_EXTENSION);
      }
    });

    assertTrue(files.length == 1);
    assertTrue(FileUtils.readFromFile(files[0]).contains(ERROR_STRING));
  }
}