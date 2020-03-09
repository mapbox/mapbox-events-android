package com.mapbox.android.core.crashreporter;

import android.content.Context;
import android.util.Log;

import androidx.annotation.RestrictTo;

import com.mapbox.android.core.BuildConfig;
import com.mapbox.android.core.FileUtils;

import java.io.File;
import java.util.Collections;

import static com.mapbox.android.core.crashreporter.Utils.ensureDirectoryWritable;
import static com.mapbox.android.core.crashreporter.Utils.getReportFileName;

public class ErrorReporter {
  private static final String LOG_TAG = "ErrorReporter";

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public static void reportError(Context context, String packageName, Throwable throwable) {
    try {
      CrashReport report = CrashReportBuilder.setup(context, packageName, BuildConfig.VERSION_NAME)
        .addCausalChain(Collections.singletonList(throwable))
        .build();

      ensureDirectoryWritable(context, packageName);

      File file = FileUtils.getFile(context, getReportFileName(packageName, report.getDateString()));
      FileUtils.writeToFile(file, report.toJson());
    } catch (Exception exception) {
      Log.e(LOG_TAG, exception.toString());
    }
  }
}
