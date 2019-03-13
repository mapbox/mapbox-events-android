package com.mapbox.android.telemetry.crash;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.mapbox.android.telemetry.MapboxTelemetry;

import java.util.HashSet;

import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_CRASH_REPORTER_PREFERENCES;
import static com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder.MAPBOX_PREF_ENABLE_CRASH_REPORTER;

final class CrashReporterClient {
  private static final String CRASH_REPORTER_CLIENT_USER_AGENT = "mapbox-android-crash";
  private final Context applicationContext;
  private final SharedPreferences sharedPreferences;
  private final MapboxTelemetry telemetry;
  private final HashSet<String> crashHashSet = new HashSet<>();

  @VisibleForTesting
  CrashReporterClient(@NonNull Context context,
                      @NonNull SharedPreferences sharedPreferences,
                      @NonNull MapboxTelemetry telemetry) {
    this.applicationContext = context;
    this.sharedPreferences = sharedPreferences;
    this.telemetry = telemetry;
  }

  static CrashReporterClient create(@NonNull Context context) {
    return new CrashReporterClient(context,
      context.getSharedPreferences(MAPBOX_CRASH_REPORTER_PREFERENCES, Context.MODE_PRIVATE),
      new MapboxTelemetry(context, "", CRASH_REPORTER_CLIENT_USER_AGENT));
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
    return false;
  }

  boolean isDuplicate(CrashEvent crashEvent) {
    return false;
  }

  CrashEvent nextEvent() {
    return null;
  }

  boolean send(CrashEvent event) {
    // Add only after event was sent
    crashHashSet.add(event.getHash());
    return false;
  }
}
