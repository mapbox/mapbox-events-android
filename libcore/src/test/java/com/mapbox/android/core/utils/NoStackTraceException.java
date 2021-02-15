package com.mapbox.android.core.utils;

public class NoStackTraceException extends Exception {
  @Override
  public synchronized Throwable fillInStackTrace() {
    return null;
  }
}
