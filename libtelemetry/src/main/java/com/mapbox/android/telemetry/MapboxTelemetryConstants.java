package com.mapbox.android.telemetry;

/**
 * Shared constants across Mapbox telemetry components.
 */
public final class MapboxTelemetryConstants {
  private MapboxTelemetryConstants() {
  }

  /**
   * Mapbox shared preferences file name
   */
  public static final String MAPBOX_SHARED_PREFERENCES = "MapboxSharedPreferences";

  /**
   * Location collector status
   */
  public static final String LOCATION_COLLECTOR_ENABLED = "mapboxTelemetryLocationState";

  /**
   * Location collector session rotation interval in milliseconds
   */
  public static final String SESSION_ROTATION_INTERVAL_MILLIS = "mapboxSessionRotationInterval";

  /**
   * Tag for Work scheduled in {@link com.mapbox.android.telemetry.crash.CrashReporterWorker}
   */
  public static final String ERROR_REPORT_WORK_TAG = "com.mapbox.android.telemetry.error.report.work";

  /**
   * Data key for input to {@link com.mapbox.android.telemetry.crash.CrashReporterWorker}
   */
  public static final String ERROR_REPORT_DATA_KEY = "com.mapbox.android.telemetry.error.data";

  /**
   * Mapbox telemetry package name (reserved for crash reporting)
   */
  public static final String MAPBOX_TELEMETRY_PACKAGE = "com.mapbox.android.telemetry";


  /**
   * Default telemetry host for STAGING Environment
   */
  @SuppressWarnings("WeakerAccess")
  public static final String DEFAULT_STAGING_EVENTS_HOST = "api-events-staging.tilestream.net";

  /**
   * Default telemetry host for COM Environment
   */
  @SuppressWarnings("WeakerAccess")
  public static final String DEFAULT_COM_EVENTS_HOST = "events.mapbox.com";

  /**
   * Default telemetry host for CHINA Environment
   */
  @SuppressWarnings("WeakerAccess")
  public static final String DEFAULT_CHINA_EVENTS_HOST = "events.mapbox.cn";
}
