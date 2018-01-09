package com.mapbox.android.telemetry;

class SessionIdentifier {
  private static final int HOURS_TO_MILLISECONDS = (60 * 60 * 1000);
  private String sessionId = null;
  private long lastSessionIdUpdate;
  private static final int DEFAULT_ROTATION_HOURS = 24;
  private int rotationInterval = DEFAULT_ROTATION_HOURS;

  SessionIdentifier() {
  }

  SessionIdentifier(int rotationInterval) {
    this.rotationInterval = rotationInterval;
  }

  String getSessionId() {
    long timeDiff = System.currentTimeMillis() - lastSessionIdUpdate;
    if (timeDiff >= rotationInterval * HOURS_TO_MILLISECONDS || sessionId == null) {
      sessionId = TelemetryUtils.obtainUniversalUniqueIdentifier();
      lastSessionIdUpdate = System.currentTimeMillis();
    }

    return sessionId;
  }
}

