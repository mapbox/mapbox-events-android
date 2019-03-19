package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mapbox.android.core.FileUtils;
import com.mapbox.android.telemetry.CrashEvent;
import com.mapbox.android.telemetry.MapboxTelemetry;
import com.mapbox.android.telemetry.TelemetryListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_CRASH_REPORTER_PREFERENCES;
import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_LAST_CRASH_REPORTER;
import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_PREF_ENABLE_CRASH_REPORTER;

final class CrashReporterClient {
  private static final String CRASH_REPORTER_CLIENT_USER_AGENT = "mapbox-android-crash";
  private final Context applicationContext;
  private final SharedPreferences sharedPreferences;
  private final MapboxTelemetry telemetry;
  private final HashSet<String> crashHashSet = new HashSet<>();
  private File[] crashReports;
  private int fileCursor;

  @VisibleForTesting
  CrashReporterClient(@NonNull Context context,
                      @NonNull SharedPreferences sharedPreferences,
                      @NonNull MapboxTelemetry telemetry,
                      File[] crashReports) {
    this.applicationContext = context;
    this.sharedPreferences = sharedPreferences;
    this.telemetry = telemetry;
    this.crashReports = crashReports;
    this.fileCursor = 0;
  }

  static CrashReporterClient create(@NonNull Context context) {
    SharedPreferences sharedPreferences =
      context.getSharedPreferences(MAPBOX_CRASH_REPORTER_PREFERENCES, Context.MODE_PRIVATE);
    return new CrashReporterClient(context, sharedPreferences,
      new MapboxTelemetry(context, "", CRASH_REPORTER_CLIENT_USER_AGENT), new File[0]);
  }

  CrashReporterClient load(@NonNull String rootPath) {
    crashReports = FileUtils.listAllFiles(applicationContext, rootPath);
    Arrays.sort(crashReports, new FileUtils.LastModifiedComparator());
    String lastSentHash = getLastSentHash();
    for (int i = 0; i < crashReports.length; i++) {
      if (lastSentHash.equals(getFileHash(i))) {
        fileCursor = i + 1;
        break;
      }
    }
    return this;
  }

  boolean isEnabled() {
    try {
      return sharedPreferences.getBoolean(MAPBOX_PREF_ENABLE_CRASH_REPORTER, false);
    } catch (Exception ex) {
      // Catch ClassCastException
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
    try {
      return parseJsonCrashEvent(FileUtils.readFromFile(crashReports[fileCursor]));
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

    AtomicBoolean success = new AtomicBoolean(false);
    CountDownLatch latch = new CountDownLatch(1);
    setupTelemetryListener(success, latch);
    telemetry.push(event);
    try {
      latch.await(10, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      return false;
    } finally {
      if (success.get()) {
        crashHashSet.add(event.getHash());
        setLastSentHash(getFileHash(fileCursor - 1));
      }
    }
    return success.get();
  }

  private String getLastSentHash() {
    try {
      return sharedPreferences.getString(MAPBOX_LAST_CRASH_REPORTER, "");
    } catch (Exception ex) {
      // Catch ClassCastException
    }
    return "";
  }

  private void setLastSentHash(String hash) {
    try {
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putString(MAPBOX_LAST_CRASH_REPORTER, hash);
      editor.apply();
    } catch (Exception ex) {
      // Catch ClassCastException
    }
  }

  private String getFileHash(int index) {
    return Long.toHexString(crashReports[index].lastModified());
  }

  private void setupTelemetryListener(final AtomicBoolean success, final CountDownLatch latch) {
    telemetry.addTelemetryListener(new TelemetryListener() {
      @Override
      public void onHttpResponse(boolean successful, int code) {
        success.set(successful);
        latch.countDown();
        telemetry.removeTelemetryListener(this);
      }

      @Override
      public void onHttpFailure(String message) {
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
      return new CrashEvent(null, null);
    }
  }
}
