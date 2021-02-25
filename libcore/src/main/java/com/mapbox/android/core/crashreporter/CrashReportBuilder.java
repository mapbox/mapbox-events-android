package com.mapbox.android.core.crashreporter;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Crash report builder encapsulates report generation logic.
 */
public final class CrashReportBuilder {
  private static final String TAG = "CrashReportBuilder";
  private static final String OS_VERSION_FORMAT = "Android-%s";
  private static final String THREAD_DETAILS_FORMAT = "tid:%s|name:%s|priority:%s";
  private static final String STACK_TRACE_ELEMENT_FORMAT = "%s.%s(%s:%d)";
  private final Context applicationContext;
  private final String sdkIdentifier;
  private final String sdkVersion;
  private final Set<String> allowedStacktracePrefixes;
  private final List<Throwable> causalChain = new ArrayList<>(4);

  private Thread uncaughtExceptionThread;
  private boolean isSilent;
  private Map<String, String> customData = null;

  private CrashReportBuilder(Context applicationContext, String sdkIdentifier, String sdkVersion,
                             Set<String> allowedStacktracePrefixes) {
    this.applicationContext = applicationContext;
    this.sdkIdentifier = sdkIdentifier;
    this.sdkVersion = sdkVersion;
    this.allowedStacktracePrefixes = allowedStacktracePrefixes;
  }

  /**
   * Exports json encoded content to CrashReport object
   *
   * @param json valid json body.
   * @return new instance of CrashReport
   */
  public static CrashReport fromJson(String json) throws IllegalArgumentException {
    try {
      return new CrashReport(json);
    } catch (JSONException je) {
      throw new IllegalArgumentException(je.toString());
    }
  }

  static CrashReportBuilder setup(Context context, String sdkIdentifier, String sdkVersion,
                                  Set<String> allowedStacktracePrefixes) {
    return new CrashReportBuilder(context, sdkIdentifier, sdkVersion, allowedStacktracePrefixes);
  }

  CrashReportBuilder isSilent(boolean silent) {
    this.isSilent = silent;
    return this;
  }

  CrashReportBuilder addCausalChain(@NonNull List<Throwable> causalChain) {
    this.causalChain.addAll(causalChain);
    return this;
  }

  CrashReportBuilder addExceptionThread(@Nullable Thread thread) {
    this.uncaughtExceptionThread = thread;
    return this;
  }

  CrashReportBuilder setCustomData(@Nullable Map<String, String> customData) {
    this.customData = customData;
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
    report.put("appId", applicationContext.getPackageName());
    report.put("appVersion", getAppVersion(applicationContext));
    report.put("customData", getCustomData(customData));
    return report;
  }

  @Nullable
  private JSONArray getCustomData(@Nullable Map<String, String> customData) {
    if (customData == null || customData.isEmpty()) {
      return null;
    }

    try {
      JSONArray jsonArray = new JSONArray();
      for (Map.Entry<String, String> entry : customData.entrySet()) {
        JSONObject keyValueObject = new JSONObject();
        keyValueObject.put("name", entry.getKey());
        keyValueObject.put("value", entry.getValue());
        jsonArray.put(keyValueObject);
      }
      return jsonArray;
    } catch (JSONException je) {
      Log.e(TAG, "Failed to create JSON array for custom data", je);
      return null;
    }
  }

  @VisibleForTesting
  @NonNull
  String getStackTrace(@NonNull List<Throwable> throwables) {
    StringBuilder result = new StringBuilder();
    for (Throwable throwable : throwables) {
      StackTraceElement[] stackTraceElements = throwable.getStackTrace();
      // Add cause message, if exception was raised in allowed package
      if (stackTraceElements.length > 0
        && isAllowedStacktraceElement(stackTraceElements[0])
        && throwable.getMessage() != null) {
        result.append(throwable.getMessage()).append('\n');
      } else {
        result.append("***\n");
      }
      // Add cause stack trace, if package is allowed
      for (StackTraceElement element : stackTraceElements) {
        if (isAllowedStacktraceElement(element)) {
          result.append(String.format(Locale.US, STACK_TRACE_ELEMENT_FORMAT,
            element.getClassName(), element.getMethodName(),
            element.getFileName(), element.getLineNumber())).append('\n');
        } else {
          result.append("*\n");
        }
      }
    }
    return result.toString();
  }

  private boolean isAllowedStacktraceElement(StackTraceElement stackTraceElement) {
    String className = stackTraceElement.getClassName();
    for (String prefix : allowedStacktracePrefixes) {
      if (className.startsWith(prefix)) {
        return true;
      }
    }
    return className.startsWith(sdkIdentifier);
  }

  @VisibleForTesting
  @NonNull
  static String getStackTraceHash(@NonNull List<Throwable> throwables) {
    StringBuilder result = new StringBuilder();
    for (Throwable throwable : throwables) {
      StackTraceElement[] stackTraceElements = throwable.getStackTrace();
      for (StackTraceElement element : stackTraceElements) {
        result.append(element.getClassName());
        result.append(element.getMethodName());
      }
    }
    return Integer.toHexString(result.toString().hashCode());
  }

  @NonNull
  private static String getAppVersion(Context context) {
    try {
      String packageName = context.getPackageName();
      return context.getPackageManager().getPackageInfo(packageName, 0).versionName;
    } catch (Exception exception) {
      return "unknown";
    }
  }
}
