package com.mapbox.android.core.crashreporter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mapbox.android.core.FileUtils;

import java.io.File;

public class Utils {

  private static final int DEFAULT_MAX_REPORTS = 10;
  private static final String CRASH_FILENAME_FORMAT = "%s/%s.crash";

  static void ensureDirectoryWritable(@NonNull Context context, @NonNull String dirPath) {
    File directory = FileUtils.getFile(context, dirPath);
    if (!directory.exists()) {
      directory.mkdir();
    }

    // Cleanup directory if we've reached our max limit
    File [] allFiles = FileUtils.listAllFiles(directory);
    if (allFiles.length >= DEFAULT_MAX_REPORTS) {
      FileUtils.deleteFirst(allFiles, new FileUtils.LastModifiedComparator(), DEFAULT_MAX_REPORTS - 1);
    }
  }

  @NonNull
  static String getReportFileName(@NonNull String mapboxPackage, @NonNull String timestamp) {
    return String.format(CRASH_FILENAME_FORMAT, mapboxPackage, timestamp);
  }
}
