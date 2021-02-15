package com.mapbox.android.core.utils;

public class ErrorTestUtils {

  public static Throwable createMapboxThrowable(String mapboxPackage) {
    Throwable throwable = createThrowable("HighLevelThrowable", "A", "B");
    throwable.initCause(createThrowable("MidLevelThrowable",
      mapboxPackage + ".A", "B", "C", "D"));
    return throwable;
  }

  public static Throwable createThrowable(String message, String... stackTraceElements) {
    StackTraceElement[] array = new StackTraceElement[stackTraceElements.length];
    for (int i = 0; i < stackTraceElements.length; i++) {
      String s = stackTraceElements[i];
      array[stackTraceElements.length - 1 - i]
        = new StackTraceElement(s, "foo", s + ".java", i);
    }
    Throwable result = new Throwable(message);
    result.setStackTrace(array);
    return result;
  }
}
