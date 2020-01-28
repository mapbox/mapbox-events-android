package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.testing.TestWorkerBuilder;

import com.mapbox.android.core.FileUtils;
import com.mapbox.android.telemetry.MapboxTelemetryConstants;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static androidx.work.ListenableWorker.Result;
import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_CRASH_REPORTER_PREFERENCES;
import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_PREF_ENABLE_CRASH_REPORTER;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_TELEMETRY_PACKAGE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CrashReporterWorkerInstrumentationTest {
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

    for (File file : directory.listFiles()) {
      file.delete();
    }
  }

  @Test
  public void checkWorkRequest() {
    assertEquals(CrashReporterWorker.createWorkRequest("") instanceof OneTimeWorkRequest, true);
  }

  @Test
  public void checkRequestConstraints() {
    OneTimeWorkRequest workRequest = CrashReporterWorker.createWorkRequest("");
    Constraints constraints = workRequest.getWorkSpec().constraints;
    assertEquals(constraints.getRequiredNetworkType(), NetworkType.CONNECTED);
  }

  // Must execute last in the test suite because test will set token
  @Test
  public void testDoWork() {
    String token = "test-token";
    CrashReporterWorker crashReporterWorker = (CrashReporterWorker)
      TestWorkerBuilder.from(context, CrashReporterWorker.class)
        .setInputData(new Data.Builder().putString(MapboxTelemetryConstants.ERROR_REPORT_DATA_KEY, token).build())
        .build();
    Result result = crashReporterWorker.doWork();
    assertThat(result, is(Result.success()));
  }

  @Test
  public void doWorkEmptyToken() {
    CrashReporterWorker crashReporterWorker = (CrashReporterWorker)
      TestWorkerBuilder.from(context, CrashReporterWorker.class)
        .setInputData(new Data.Builder().putString(MapboxTelemetryConstants.ERROR_REPORT_DATA_KEY, "").build())
        .build();
    Result result = crashReporterWorker.doWork();
    assertThat(result, is(Result.failure()));
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

    CrashReporterWorker crashReporterWorker = TestWorkerBuilder.from(context, CrashReporterWorker.class).build();
    crashReporterWorker.handleCrashReports(CrashReporterClient
      .create(context, "")
      .loadFrom(directory)
      .debug(true));
    assertEquals(0, FileUtils.listAllFiles(directory).length);
  }
}
