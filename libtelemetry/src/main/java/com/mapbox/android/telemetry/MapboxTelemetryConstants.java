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
   * Intent broadcast when valid token becomes available
   */
  public static final String ACTION_TOKEN_CHANGED = "com.mapbox.android.telemetry.action.TOKEN_CHANGED";
}
