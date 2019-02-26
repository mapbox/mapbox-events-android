package com.mapbox.android.telemetry.location;

import com.mapbox.android.telemetry.TelemetryUtils;

public class SessionIdentifier {
  private static final long HOURS_TO_MILLISECONDS = 60 * 60 * 1000;
  private static final int DEFAULT_ROTATION_HOURS = 24;
  private final long rotationInterval;
  private String sessionId = null;
  private long lastSessionIdUpdate;

  public SessionIdentifier() {
    this(DEFAULT_ROTATION_HOURS * HOURS_TO_MILLISECONDS);
  }

  public SessionIdentifier(long intervalMillis) {
    this.rotationInterval = intervalMillis;
  }

  public SessionIdentifier(int rotationInterval) {
    this.rotationInterval = rotationInterval * HOURS_TO_MILLISECONDS;
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

