package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.platform.app.InstrumentationRegistry;
import com.mapbox.android.core.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_CRASH_REPORTER_PREFERENCES;
import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_PREF_ENABLE_CRASH_REPORTER;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_TELEMETRY_PACKAGE;
import static org.junit.Assert.assertEquals;

public class CrashReporterJobIntentServiceInstrumentationTest {
  private static final String CRASH_FILENAME_FORMAT = "%s/%s.crash";
  private static final String crashEvent =
    "{\"event\":\"mobile.crash\",\"created\":\"2019-02-21T21:58:43.000Z\",\"stackTraceHash\":\"%s\"}";

  private File directory;
  private Context context;

  @Before
  public void setUp() {
    context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    directory = FileUtils.getFile(context, MAPBOX_TELEMETRY_PACKAGE);
    if (!directory.exists()) {
      directory.mkdir();
    }

    for (File file: directory.listFiles()) {
      file.delete();
    }
  }

  @Test
  public void enqueueWork() {
    CrashReporterJobIntentService.enqueueWork(InstrumentationRegistry.getInstrumentation().getTargetContext());
    // TODO: verify work is executed
  }

  @Test
  public void handleCrashReports() throws IOException {
    SharedPreferences sharedPreferences =
      context.getSharedPreferences(MAPBOX_CRASH_REPORTER_PREFERENCES, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(MAPBOX_PREF_ENABLE_CRASH_REPORTER, true);
    editor.commit();

    File file = FileUtils.getFile(context, String.format(CRASH_FILENAME_FORMAT, MAPBOX_TELEMETRY_PACKAGE, "crash1"));
    FileUtils.writeToFile(file, String.format(crashEvent, UUID.randomUUID().toString()));

    CrashReporterJobIntentService jobIntentService = new CrashReporterJobIntentService();
    jobIntentService.handleCrashReports(CrashReporterClient
      .create(InstrumentationRegistry.getInstrumentation().getTargetContext())
      .loadFrom(directory)
      .debug(true));
    assertEquals(0, FileUtils.listAllFiles(directory).length);
  }
}