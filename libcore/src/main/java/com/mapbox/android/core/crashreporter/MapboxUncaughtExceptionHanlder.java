package com.mapbox.android.core.crashreporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mapbox.android.core.FileUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapboxUncaughtExceptionHanlder implements Thread.UncaughtExceptionHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String MAPBOX_PREF_ENABLE_CRASH_REPORTER = "mapbox.crash.enable";
    public static final String MAPBOX_CRASH_REPORTER_PREFERENCES = "MapboxCrashReporterPrefs";

    private static final String TAG = "MbUncaughtExcHandler";
    private static final String CRASH_FILENAME_FORMAT = "%s/%s.crash";

    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private final Context applicationContext;
    private final AtomicBoolean isEnabled = new AtomicBoolean(true);
    private final String mapboxPackage;

    private MapboxUncaughtExceptionHanlder(Context context,
                                           String mapboxPackage,
                                           Thread.UncaughtExceptionHandler defaultExceptionHandler) {
        this.applicationContext = context;
        this.mapboxPackage = mapboxPackage;
        this.defaultExceptionHandler = defaultExceptionHandler;

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(MAPBOX_CRASH_REPORTER_PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Installs exception handler for particular Mapbox module/sdk
     *
     * Crash data will land in context.getFilesDir()/${mapboxPackage}/
     *
     * @param context application context.
     * @param mapboxPackage mapbox package name exceptions to handle.
     *
     * Note: Package name can be used to control coverage: i.e. `com.mapbox` will catch all
     *       mapbox exceptions in the context of a single app process.
     */
    public static void install(@Nullable Context context, String mapboxPackage) {
        if (context == null) {
            Log.i(TAG, "Uncaught exception handler cannot be installed: context == null");
            return;
        }

        Thread.setDefaultUncaughtExceptionHandler(new MapboxUncaughtExceptionHanlder(context,
                mapboxPackage, Thread.getDefaultUncaughtExceptionHandler()));
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // If we're not enabled or crash is not in Mapbox code
        // then just pass the Exception on to the defaultExceptionHandler.
        if (isEnabled.get() && isMapboxCrash(e)) {
            // Handle Mapbox exception
            try {
                // TODO: capture crash data
                String timestamp = "";
                String json = "";

                // TODO: create file
                File file = FileUtils.getFile(applicationContext,
                        getReportFileName(mapboxPackage, timestamp));

                if (!file.exists()) {
                    // Create file
                }

                // TODO: write data to disk
                FileUtils.writeToFile(file, json);
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }

        // Give default exception handler a chance to handle exception
        if (defaultExceptionHandler != null) {
            defaultExceptionHandler.uncaughtException(t, e);
        } else {
            Log.i(TAG, "Default exception handler is null");
        }
    }

    private boolean isMapboxCrash(@Nullable Throwable th) {
        Throwable cause = th;
        while (cause != null) {
            final StackTraceElement[] stackTraceElements = cause.getStackTrace();
            for (final StackTraceElement e : stackTraceElements) {
                String className = e.getClassName();
                if (className.startsWith(mapboxPackage)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

    @NonNull
    private static String getReportFileName(@NonNull String mapboxPackage,
                                            @NonNull String timestamp) {
        return String.format(CRASH_FILENAME_FORMAT, mapboxPackage, timestamp);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            isEnabled.set(sharedPreferences.getBoolean(MAPBOX_PREF_ENABLE_CRASH_REPORTER, false));
        } catch (Exception e) {
            // In case of a ClassCastException
            Log.e(TAG, e.toString());
        }
    }
}
