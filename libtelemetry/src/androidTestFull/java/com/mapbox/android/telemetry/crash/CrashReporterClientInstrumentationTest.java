package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.platform.app.InstrumentationRegistry;
import com.mapbox.android.core.FileUtils;
import com.mapbox.android.telemetry.CrashEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_CRASH_REPORTER_PREFERENCES;
import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_PREF_ENABLE_CRASH_REPORTER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CrashReporterClientInstrumentationTest {
  private static final String TEST_DIR_PATH = "com.mapbox.android.telemetry.test";
  private static final String CRASH_FILENAME_FORMAT = "%s/%s.crash";
  private static final String crashEvent =
    "{\"event\":\"mobile.crash\",\"created\":\"2019-02-21T21:58:43.000Z\",\"stackTraceHash\":\"%s\"}";

  private File directory;
  private CrashReporterClient crashReporterClient;
  private Context context;

  @Before
  public void setUp() {
    context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    directory = FileUtils.getFile(context, TEST_DIR_PATH);
    if (!directory.exists()) {
      directory.mkdir();
    }

    for (File file: directory.listFiles()) {
      file.delete();
    }
    crashReporterClient = CrashReporterClient.create(context, "");
  }

  @Test
  public void loadInvalidNullPath() {
    CrashReporterClient client = crashReporterClient.loadFrom(null);
    assertFalse(client.hasNextEvent());
  }

  @Test
  public void loadInvalidPath() {
    CrashReporterClient client = crashReporterClient.loadFrom(new File(""));
    assertFalse(client.hasNextEvent());
  }

  @Test
  public void verifyEventNotLoadedFromEmptyPath() {
    CrashReporterClient client = crashReporterClient.loadFrom(directory);
    assertFalse(client.hasNextEvent());
  }

  @Test
  public void verifyEventLoadedWithValidHash() throws IOException {
    String crashHash = UUID.randomUUID().toString();
    File file = FileUtils.getFile(context, String.format(CRASH_FILENAME_FORMAT, TEST_DIR_PATH, crashHash));
    FileUtils.writeToFile(file, String.format(crashEvent, crashHash));

    CrashReporterClient client = crashReporterClient.loadFrom(directory);
    assertTrue(client.hasNextEvent());
  }

  @Test
  public void verifyEventLoadedFirstTime() throws IOException {
    String crashHash = UUID.randomUUID().toString();
    File file = FileUtils.getFile(context, String.format(CRASH_FILENAME_FORMAT, TEST_DIR_PATH, crashHash));
    FileUtils.writeToFile(file, String.format(crashEvent, crashHash));

    CrashReporterClient client = crashReporterClient.loadFrom(directory);
    assertTrue(client.hasNextEvent());
  }

  @Test
  public void verifyFileCursorReset() throws IOException {
    String crashHash = UUID.randomUUID().toString();
    File file = FileUtils.getFile(context, String.format(CRASH_FILENAME_FORMAT, TEST_DIR_PATH, crashHash));
    FileUtils.writeToFile(file, String.format(crashEvent, crashHash));

    CrashReporterClient client = crashReporterClient.loadFrom(directory);
    assertTrue(client.hasNextEvent());

    client = crashReporterClient.loadFrom(new File(""));
    assertFalse(client.hasNextEvent());
  }

  @Test
  public void verifyDisabledState() {
    SharedPreferences sharedPreferences = getSharedPreferences();
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(MAPBOX_PREF_ENABLE_CRASH_REPORTER, false);
    editor.commit();
    assertFalse(crashReporterClient.isEnabled());
  }

  @Test
  public void verifyEnabledState() {
    SharedPreferences sharedPreferences = getSharedPreferences();
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(MAPBOX_PREF_ENABLE_CRASH_REPORTER, true);
    editor.commit();
    assertTrue(crashReporterClient.isEnabled());
  }

  @Test
  public void filterDupEventsBasedOnHash() throws IOException {
    String crashHash = UUID.randomUUID().toString();
    File file = FileUtils.getFile(context, String.format(CRASH_FILENAME_FORMAT, TEST_DIR_PATH, "crash1"));
    FileUtils.writeToFile(file, String.format(crashEvent, crashHash));

    file = FileUtils.getFile(context, String.format(CRASH_FILENAME_FORMAT, TEST_DIR_PATH, "crash2"));
    FileUtils.writeToFile(file, String.format(crashEvent, crashHash));

    // Need to toggle this flag to simulate telem success
    crashReporterClient.loadFrom(directory);
    CrashEvent event = crashReporterClient.nextEvent();
    assertFalse(crashReporterClient.isDuplicate(event));

    AtomicBoolean success = new AtomicBoolean(true);
    CountDownLatch latch = new CountDownLatch(1);
    crashReporterClient.sendSync(event, success, latch);

    event = crashReporterClient.nextEvent();
    assertTrue(crashReporterClient.isDuplicate(event));
  }

  @Test(expected = IllegalStateException.class)
  public void nextCalledPriorLoadEvent() {
    crashReporterClient.nextEvent();
  }

  @Test
  public void verifyMappedFileDeleted() throws IOException {
    /* File fileCrash1 = FileUtils.getFile(context, String.format(CRASH_FILENAME_FORMAT, TEST_DIR_PATH, "crash1"));
    FileUtils.writeToFile(fileCrash1, String.format(crashEvent, UUID.randomUUID().toString()));

    File fileCrash2 = FileUtils.getFile(context, String.format(CRASH_FILENAME_FORMAT, TEST_DIR_PATH, "crash2"));
    FileUtils.writeToFile(fileCrash2, String.format(crashEvent, UUID.randomUUID().toString()));

    crashReporterClient.loadFrom(directory);
    crashReporterClient.nextEvent();
    CrashEvent crash2 = crashReporterClient.nextEvent();
    crashReporterClient.delete(crash2);

    assertTrue(fileCrash1.exists());
    assertFalse(fileCrash2.exists()); */
  }

  @Test
  public void deleteInvalidEvent() {
    crashReporterClient.loadFrom(directory);
    assertFalse(crashReporterClient.delete(new CrashEvent("mobile.crash", "2019-02-21T21:58:43.000Z")));
  }

  @Test
  public void attemptToDeleteAlreadyDeletedFile() throws IOException {
    File file = FileUtils.getFile(context, String.format(CRASH_FILENAME_FORMAT, TEST_DIR_PATH, "crash"));
    FileUtils.writeToFile(file, String.format(crashEvent, UUID.randomUUID().toString()));
    crashReporterClient.loadFrom(directory);
    CrashEvent crashEvent = crashReporterClient.nextEvent();

    assertTrue(file.delete());
    assertFalse(crashReporterClient.delete(crashEvent));
  }

  private SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences(MAPBOX_CRASH_REPORTER_PREFERENCES, Context.MODE_PRIVATE);
  }
}