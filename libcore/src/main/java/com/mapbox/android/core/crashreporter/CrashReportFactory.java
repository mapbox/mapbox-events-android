package com.mapbox.android.core.crashreporter;

import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Factory for creating {@link CrashReport}.
 */
public final class CrashReportFactory {
  private static final int DEFAULT_CRASH_CHAIN_DEPTH = 2;
  private static final int DEFAULT_NON_FATAL_ERROR_CHAIN_DEPTH = 1;

  private final Context applicationContext;
  private final String mapboxPackage;
  private final String mapboxModuleVersion;
  private final Set<String> allowedStacktracePrefixes;

  private int crashChainDepth;

  public CrashReportFactory(Context applicationContext, String mapboxPackage, String mapboxModuleVersion,
                            Set<String> allowedStacktracePrefixes) {
    this.applicationContext = applicationContext;
    this.mapboxPackage = mapboxPackage;
    this.mapboxModuleVersion = mapboxModuleVersion;
    this.allowedStacktracePrefixes = allowedStacktracePrefixes;
    crashChainDepth = DEFAULT_CRASH_CHAIN_DEPTH;
  }

  /**
   * Create {@link CrashReport} for crash.
   *
   * @param thread thread, where exception has occurred
   * @param throwable crash itself
   * @return report, if crash was partly or fully caused by Mapbox module/sdk with
   *         {@linkplain CrashReportFactory#mapboxPackage} package name; otherwise, null
   *         is returned
   */
  @Nullable
  public CrashReport createReportForCrash(Thread thread, Throwable throwable) {
    CrashReport report = null;

    List<Throwable> causalChain;
    if (isMapboxCrash(causalChain = getCausalChain(throwable, crashChainDepth))) {
      report = CrashReportBuilder
        .setup(applicationContext, mapboxPackage, mapboxModuleVersion, allowedStacktracePrefixes)
        .addExceptionThread(thread)
        .addCausalChain(causalChain)
        .build();
    }

    return report;
  }

  /**
   * Create {@link CrashReport} for non-fatal error.
   *
   * @param throwable non-fatal error
   * @return report, if non-fatal error was partly or fully caused by Mapbox module/sdk with
   *         {@linkplain CrashReportFactory#mapboxPackage} package name; otherwise, null
   *         is returned
   */
  @Nullable
  public CrashReport createReportForNonFatal(Throwable throwable) {
    CrashReport report = null;

    List<Throwable> causalChain;
    if (isMapboxCrash(causalChain = getCausalChain(throwable, DEFAULT_NON_FATAL_ERROR_CHAIN_DEPTH))) {
      report = CrashReportBuilder
        .setup(applicationContext, mapboxPackage, mapboxModuleVersion, allowedStacktracePrefixes)
        .isSilent(true)
        .addCausalChain(causalChain)
        .build();
    }

    return report;
  }

  /**
   * Set exception chain depth we're interested in to dig into backtrace data.
   *
   * @param depth of exception chain
   */
  @VisibleForTesting
  void setCrashChainDepth(@IntRange(from = 1, to = 256) int depth) {
    this.crashChainDepth = depth;
  }

  @VisibleForTesting
  boolean isMapboxCrash(List<Throwable> throwables) {
    for (Throwable cause : throwables) {
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
  List<Throwable> getCausalChain(@Nullable Throwable throwable, int causeDepth) {
    List<Throwable> causes = new ArrayList<>(4);
    int level = 0;
    while (throwable != null) {
      if (++level >= causeDepth) {
        causes.add(throwable);
      }
      throwable = throwable.getCause();
    }
    return Collections.unmodifiableList(causes);
  }

  private boolean isMapboxStackTraceElement(@NonNull StackTraceElement element) {
    return element.getClassName().startsWith(mapboxPackage);
  }
}
