package com.mapbox.android.telemetry.location;

/**
 * Location controller attached to activity lifecycle events
 */
interface LocationEngineController {
  void setSessionIdentifier(SessionIdentifier sessionIdentifier);

  void onPause();

  void onResume();

  void onDestroy();
}
