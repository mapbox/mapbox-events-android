package com.mapbox.android.core.crashreporter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public final class CrashReportBuilder {
  private static final String OS_VERSION_FORMAT = "Android-%s";
  private static final String THREAD_DETAILS_FORMAT = "tid:%s|name:%s|priority:%s";
  private static final String STACK_TRACE_ELEMENT_FORMAT = "%s.%s(%s:%d)";
  private final Context applicationContext;
  private final String sdkIdentifier;
  private final String sdkVersion;
  private final List<Throwable> causalChain = new ArrayList<>(4);

  private Thread uncaughtExceptionThread;
  private boolean isSilent;

  private CrashReportBuilder(Context applicationContext, String sdkIdentifier, String sdkVersion) {
    this.applicationContext = applicationContext;
    this.sdkIdentifier = sdkIdentifier;
    this.sdkVersion = sdkVersion;
  }

  public static CrashReport fromJson(String json) throws IllegalArgumentException {
    try {
      return new CrashReport(json);
    } catch (JSONException je) {
      throw new IllegalArgumentException(je.toString());
    }
  }

  static CrashReportBuilder setup(Context context, String sdkIdentifier, String sdkVersion) {
    return new CrashReportBuilder(context, sdkIdentifier, sdkVersion);
  }

  CrashReportBuilder isSilent(boolean silent) {
    this.isSilent = silent;
    return this;
  }

  CrashReportBuilder addCausalChain(@NonNull List<Throwable> causalChain) {
    this.causalChain.addAll(causalChain);
    return this;
  }

  CrashReportBuilder addExceptionThread(@NonNull Thread thread) {
    this.uncaughtExceptionThread = thread;
    return this;
  }

  CrashReport build() {
    CrashReport report = new CrashReport(new GregorianCalendar());
    report.put("sdkIdentifier", sdkIdentifier);
    report.put("sdkVersion", sdkVersion);
    report.put("osVersion", String.format(OS_VERSION_FORMAT, Build.VERSION.RELEASE));
    report.put("model", Build.MODEL);
    report.put("device", Build.DEVICE);
    report.put("isSilent", Boolean.toString(isSilent));
    report.put("stackTraceHash", getStackTraceHash(causalChain));
    report.put("stackTrace", getStackTrace(causalChain));
    if (uncaughtExceptionThread != null) {
      report.put("threadDetails", String.format(THREAD_DETAILS_FORMAT, uncaughtExceptionThread.getId(),
        uncaughtExceptionThread.getName(), uncaughtExceptionThread.getPriority()));
    }
    report.put("appID", applicationContext.getPackageName());
    return report;
  }

  @VisibleForTesting
  @NonNull
  String getStackTrace(@NonNull List<Throwable> throwables) {
    StringBuilder result = new StringBuilder();
    for (Throwable throwable: throwables) {
      StackTraceElement[] stackTraceElements = throwable.getStackTrace();
      for (StackTraceElement element: stackTraceElements) {
        if (element.getClassName().startsWith(sdkIdentifier)) {
          result.append(String.format(Locale.US, STACK_TRACE_ELEMENT_FORMAT,
            element.getClassName(), element.getMethodName(),
            element.getFileName(), element.getLineNumber())).append('\n');
        }
      }
    }
    return result.toString();
  }

  @VisibleForTesting
  @NonNull
  static String getStackTraceHash(@NonNull List<Throwable> throwables) {
    StringBuilder result = new StringBuilder();
    for (Throwable throwable: throwables) {
      StackTraceElement[] stackTraceElements = throwable.getStackTrace();
      for (StackTraceElement element: stackTraceElements) {
        result.append(element.getClassName());
        result.append(element.getMethodName());
      }
    }
    return Integer.toHexString(result.toString().hashCode());
  }
}
