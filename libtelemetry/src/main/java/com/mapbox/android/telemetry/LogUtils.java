package com.mapbox.android.telemetry;

import android.util.Log;

public class LogUtils {
  private static final int maxLogLevel = Log.DEBUG;

  public static boolean isLoggable(String tag, int level) {
    if (maxLogLevel > level) {
      return false;
    }
    return BuildConfig.DEBUG || Log.isLoggable(tag, level);
  }

  public static int v(String tag, String format, Object... args) {
    return isLoggable(tag, Log.VERBOSE)
        ? Log.v(tag, String.format(format, args)) : 0;
  }

  public static int d(String tag, String format, Object... args) {
    return isLoggable(tag, Log.DEBUG)
        ? Log.d(tag, String.format(format, args)) : 0;
  }

  private static int i(String tag, String format, Object... args) {
    return isLoggable(tag, Log.INFO)
        ? Log.i(tag, String.format(format, args)) : 0;
  }

  public static int w(String tag, String format, Object... args) {
    return isLoggable(tag, Log.WARN)
        ? Log.w(tag, String.format(format, args)) : 0;
  }

  public static int w(String tag, Throwable tr, String format, Object... args) {
    return isLoggable(tag, Log.WARN)
        ? Log.w(tag, String.format(format, args), tr) : 0;
  }

  public static int e(String tag, String format, Object... args) {
    return isLoggable(tag, Log.ERROR)
        ? Log.e(tag, String.format(format, args)) : 0;
  }

  public static int e(String tag, Throwable tr, String format, Object... args) {
    return isLoggable(tag, Log.ERROR)
        ? Log.e(tag, String.format(format, args), tr) : 0;
  }
}
