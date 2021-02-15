package com.mapbox.android.telemetry;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.mapbox.android.core.crashreporter.CrashReport;
import com.mapbox.android.core.crashreporter.CrashReportFactory;
import com.mapbox.android.telemetry.errors.ErrorUtils;

import java.util.Set;

/**
 * Class for reporting {@link Throwable} via Telemetry.
 */
public class MapboxCrashReporter {

  private final MapboxTelemetry telemetry;
  private final CrashReportFactory crashReportFactory;

  public MapboxCrashReporter(@NonNull MapboxTelemetry telemetry,
                             @NonNull String mapboxPackage,
                             @NonNull String mapboxModuleVersion,
                             @NonNull Set<String> allowedStacktracePrefixes) {
    this.telemetry = telemetry;
    crashReportFactory = new CrashReportFactory(MapboxTelemetry.applicationContext,
      mapboxPackage, mapboxModuleVersion, allowedStacktracePrefixes);
  }

  // For testing only
  MapboxCrashReporter(@NonNull MapboxTelemetry telemetry,
                      @NonNull CrashReportFactory crashReportFactory) {
    this.telemetry = telemetry;
    this.crashReportFactory = crashReportFactory;
  }

  /**
   * Report non-fatal exception via Telemetry.
   *
   * @param throwable non-fatal error, that should be reported
   * @return whether error event was added to event queue
   */
  public boolean reportError(Throwable throwable) {
    CrashReport report = crashReportFactory.createReportForNonFatal(throwable);
    if (report != null) {
      CrashEvent nonFatalErrorEvent = parseReportAsEvent(report);
      return telemetry.push(nonFatalErrorEvent);
    }
    return false;
  }

  @VisibleForTesting
  CrashEvent parseReportAsEvent(CrashReport report) {
    return ErrorUtils.parseJsonCrashEvent(report.toJson());
  }
}
