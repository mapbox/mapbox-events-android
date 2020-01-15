package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mapbox.android.core.FileUtils;
import com.mapbox.android.telemetry.BuildConfig;
import com.mapbox.android.telemetry.CrashEvent;
import com.mapbox.android.telemetry.MapboxTelemetry;
import com.mapbox.android.telemetry.TelemetryListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_CRASH_REPORTER_PREFERENCES;
import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_PREF_ENABLE_CRASH_REPORTER;

final class CrashReporterClient {
  private static final String LOG_TAG = "CrashReporterClient";
  private static final String CRASH_REPORTER_CLIENT_USER_AGENT = "mapbox-android-crash";
  private final SharedPreferences sharedPreferences;
  private final MapboxTelemetry telemetry;
  private final HashSet<String> crashHashSet = new HashSet<>();
  private final HashMap<CrashEvent, File> eventFileHashMap = new HashMap<>();
  private File[] crashReports;
  private int fileCursor;
  private boolean isDebug;

  @VisibleForTesting
  CrashReporterClient(@NonNull SharedPreferences sharedPreferences,
                      @NonNull MapboxTelemetry telemetry,
                      File[] crashReports) {
    this.sharedPreferences = sharedPreferences;
    this.telemetry = telemetry;
    this.crashReports = crashReports;
    this.fileCursor = 0;
    this.isDebug = false;
  }

  static CrashReporterClient create(@NonNull Context context, String accessToken) {
    SharedPreferences sharedPreferences =
      context.getSharedPreferences(MAPBOX_CRASH_REPORTER_PREFERENCES, Context.MODE_PRIVATE);
    return new CrashReporterClient(sharedPreferences,
      new MapboxTelemetry(context, accessToken,
        String.format("%s/%s", CRASH_REPORTER_CLIENT_USER_AGENT, BuildConfig.VERSION_NAME), false), new File[0]);
  }

  CrashReporterClient loadFrom(@NonNull File rootDir) {
    fileCursor = 0;
    crashReports = FileUtils.listAllFiles(rootDir);
    Arrays.sort(crashReports, new FileUtils.LastModifiedComparator());
    return this;
  }

  CrashReporterClient debug(boolean isDebug) {
    this.isDebug = isDebug;
    return this;
  }

  boolean isEnabled() {
    try {
      return sharedPreferences.getBoolean(MAPBOX_PREF_ENABLE_CRASH_REPORTER, true);
    } catch (Exception ex) {
      // Catch ClassCastException
      Log.e(LOG_TAG, ex.toString());
      return false;
    }
  }

  boolean hasNextEvent() {
    return fileCursor < crashReports.length;
  }

  boolean isDuplicate(CrashEvent crashEvent) {
    return crashHashSet.contains(crashEvent.getHash());
  }

  @NonNull
  CrashEvent nextEvent() {
    if (!hasNextEvent()) {
      throw new IllegalStateException("No more events can be read");
    }

    try {
      File file = crashReports[fileCursor];
      CrashEvent event = parseJsonCrashEvent(FileUtils.readFromFile(file));
      if (event.isValid()) {
        eventFileHashMap.put(event, file);
      }
      return event;
    } catch (FileNotFoundException fileException) {
      throw new IllegalStateException("File cannot be read: " + fileException.toString());
    } finally {
      fileCursor++;
    }
  }

  boolean send(CrashEvent event) {
    if (!event.isValid()) {
      return false;
    }
    AtomicBoolean success = new AtomicBoolean(isDebug);
    CountDownLatch latch = new CountDownLatch(1);
    return sendSync(event, success, latch);
  }

  boolean delete(CrashEvent event) {
    File file = eventFileHashMap.get(event);
    return file != null && file.delete();
  }

  @VisibleForTesting
  boolean sendSync(CrashEvent event, AtomicBoolean status, CountDownLatch latch) {
    setupTelemetryListener(status, latch);
    telemetry.push(event);
    try {
      latch.await(10, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      return false;
    } finally {
      if (status.get()) {
        crashHashSet.add(event.getHash());
      } else {
        // TODO: implement retry policy
      }
    }
    return status.get();
  }

  private void setupTelemetryListener(final AtomicBoolean success, final CountDownLatch latch) {
    telemetry.addTelemetryListener(new TelemetryListener() {
      @Override
      public void onHttpResponse(boolean successful, int code) {
        Log.d(LOG_TAG, "Response: " + code);
        success.set(successful);
        latch.countDown();
        telemetry.removeTelemetryListener(this);
      }

      @Override
      public void onHttpFailure(String message) {
        Log.d(LOG_TAG, "Response: " + message);
        latch.countDown();
        telemetry.removeTelemetryListener(this);
      }
    });
  }

  private static CrashEvent parseJsonCrashEvent(String json) {
    Gson gson = new GsonBuilder().create();
    try {
      return gson.fromJson(json, CrashEvent.class);
    } catch (JsonSyntaxException jse) {
      Log.e(LOG_TAG, jse.toString());
      return new CrashEvent(null, null);
    }
  }
}