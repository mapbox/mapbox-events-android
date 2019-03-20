package com.mapbox.android.telemetry.location;

import com.mapbox.android.telemetry.TelemetryUtils;

/**
 * Session identifier is managing session id renewal policy.
 * <p>
 * Will be deprecated in future releases - avoid creating dependencies of this class.
 */
public class SessionIdentifier {
  private static final long HOURS_TO_MILLISECONDS = 60 * 60 * 1000;
  private static final int DEFAULT_ROTATION_HOURS = 24;
  private final long rotationInterval;
  private String sessionId = null;
  private long lastSessionIdUpdate;

  /**
   * By default session id renewal interval is 24 hours.
   */
  public SessionIdentifier() {
    this(DEFAULT_ROTATION_HOURS * HOURS_TO_MILLISECONDS);
  }

  /**
   * Create instance of session identifier object.
   *
   * @param intervalMillis interval in milliseconds.
   */
  public SessionIdentifier(long intervalMillis) {
    this.rotationInterval = intervalMillis;
  }

  /**
   * Create instance of session identifier object.
   *
   * @param rotationInterval interval in hours.
   */
  @Deprecated
  public SessionIdentifier(int rotationInterval) {
    this.rotationInterval = rotationInterval * HOURS_TO_MILLISECONDS;
  }

  /**
   * Return rotation interval in milliseconds
   *
   * @return interval in milliseconds
   */
  public long getInterval() {
    return rotationInterval;
  }

  String getSessionId() {
    long timeDiff = System.currentTimeMillis() - lastSessionIdUpdate;
    if (timeDiff >= rotationInterval || sessionId == null) {
      sessionId = TelemetryUtils.obtainUniversalUniqueIdentifier();
      lastSessionIdUpdate = System.currentTimeMillis();
    }
    return sessionId;
  }
}

