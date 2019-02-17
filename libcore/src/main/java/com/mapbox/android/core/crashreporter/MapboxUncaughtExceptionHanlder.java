package com.mapbox.android.core.crashreporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.mapbox.android.core.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapboxUncaughtExceptionHanlder implements Thread.UncaughtExceptionHandler,
  SharedPreferences.OnSharedPreferenceChangeListener {
  public static final String MAPBOX_PREF_ENABLE_CRASH_REPORTER = "mapbox.crash.enable";
  public static final String MAPBOX_CRASH_REPORTER_PREFERENCES = "MapboxCrashReporterPrefs";
  public static final int DEFAULT_EXCEPTION_CHAIN_DEPTH = 3;

  private static final String TAG = "MbUncaughtExcHandler";
  private static final String CRASH_FILENAME_FORMAT = "%s/%s.crash";

  private final Thread.UncaughtExceptionHandler defaultExceptionHandler;
  private final Context applicationContext;
  private final AtomicBoolean isEnabled = new AtomicBoolean(true);
  private final String mapboxPackage;

  private int exceptionChainDepth;

  @VisibleForTesting
  MapboxUncaughtExceptionHanlder(@NonNull Context applicationContext,
                                 @NonNull SharedPreferences sharedPreferences,
                                 @NonNull String mapboxPackage,
                                 Thread.UncaughtExceptionHandler defaultExceptionHandler) {
    if (mapboxPackage == null || mapboxPackage.isEmpty()) {
      throw new IllegalArgumentException("Invalid package name: " + mapboxPackage);
    }
    this.applicationContext = applicationContext;
    this.mapboxPackage = mapboxPackage;
    this.exceptionChainDepth = DEFAULT_EXCEPTION_CHAIN_DEPTH;
    this.defaultExceptionHandler = defaultExceptionHandler;
    sharedPreferences.registerOnSharedPreferenceChangeListener(this);
  }

  /**
   * Installs exception handler for Mapbox module/sdk
   * <p>
   * Crash data will land in context.getFilesDir()/${mapboxPackage}/
   *
   * @param context        application context.
   * @param mapboxPackage  mapbox package name exceptions to handle.
   *
   * <p>
   * Note: Package name used to filter exceptions: i.e. `com.mapbox.android.telemetry` will catch all
   *       telemetry exceptions in the context of a single app process.
   */
  public static void install(@NonNull Context context, @NonNull String mapboxPackage) {
    Context applicationContext;
    if (context.getApplicationContext() == null) {
      // In shared processes content providers getApplicationContext() can return null.
      applicationContext = context;
    } else {
      applicationContext = context.getApplicationContext();
    }

    Thread.setDefaultUncaughtExceptionHandler(new MapboxUncaughtExceptionHanlder(applicationContext,
      applicationContext.getSharedPreferences(MAPBOX_CRASH_REPORTER_PREFERENCES, Context.MODE_PRIVATE),
      mapboxPackage, Thread.getDefaultUncaughtExceptionHandler()));
  }

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    // If we're not enabled or crash is not in Mapbox code
    // then just pass the Exception on to the defaultExceptionHandler.
    List<Throwable> causalChain;
    if (isEnabled.get() && isMapboxCrash(causalChain = getCausalChain(throwable))) {
      try {
        CrashReport report = CrashReportBuilder.setup(applicationContext, mapboxPackage)
          .addExceptionThread(thread)
          .addCausalChain(causalChain)
          .build();

        File file = FileUtils.getFile(applicationContext, getReportFileName(mapboxPackage, report.getDateString()));
        FileUtils.writeToFile(file, report.toJson());
      } catch (Exception ex) {
        Log.e(TAG, ex.toString());
      }
    }

    // Give default exception handler a chance to handle exception
    if (defaultExceptionHandler != null) {
      defaultExceptionHandler.uncaughtException(thread, throwable);
    } else {
      Log.i(TAG, "Default exception handler is null");
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    try {
      isEnabled.set(sharedPreferences.getBoolean(MAPBOX_PREF_ENABLE_CRASH_REPORTER, false));
    } catch (Exception ex) {
      // In case of a ClassCastException
      Log.e(TAG, ex.toString());
    }
  }

  /**
   * Set exception chain depth we're interested in to dig into backtrace data.
   *
   * @param depth of exception chain
   */
  @VisibleForTesting
  void setExceptionChainDepth(@IntRange(from=1, to=256) int depth) {
    this.exceptionChainDepth = depth;
  }

  @VisibleForTesting
  boolean isMapboxCrash(List<Throwable> throwables) {
    for (Throwable cause: throwables) {
      final StackTraceElement[] stackTraceElements = cause.getStackTrace();
      for (final StackTraceElement element : stackTraceElements) {
        if (isMapboxStackTraceElement(element)) {
          return true;
        }
      }
    }
    return false;
  }

  @VisibleForTesting
  List<Throwable> getCausalChain(@Nullable Throwable throwable) {
    List<Throwable> causes = new ArrayList<>(4);
    int level = 0;
    while (throwable != null) {
      if (isMidOrLowLevelException(++level)) {
        causes.add(throwable);
      }
      throwable = throwable.getCause();
    }
    return Collections.unmodifiableList(causes);
  }

  private boolean isMapboxStackTraceElement(@NonNull StackTraceElement element) {
    return element.getClassName().startsWith(mapboxPackage);
  }

  private boolean isMidOrLowLevelException(int level) {
    return level >= exceptionChainDepth;
  }

  @NonNull
  private static String getReportFileName(@NonNull String mapboxPackage,
                                          @NonNull String timestamp) {
    return String.format(CRASH_FILENAME_FORMAT, mapboxPackage, timestamp);
  }
}
