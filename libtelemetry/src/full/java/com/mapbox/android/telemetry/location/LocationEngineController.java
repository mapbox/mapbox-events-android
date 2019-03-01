package com.mapbox.android.telemetry.location;

import android.app.Activity;

/**
 * Location controller attached to activity lifecycle events
 */
interface LocationEngineController {
  /**
   * Allows changing session rotation interval by providing a new
   * version of session identifier object, which manages this policy.
   *
   * @param sessionIdentifier reference to the new session identified object
   */
  void setSessionIdentifier(SessionIdentifier sessionIdentifier);

  /**
   * Called when {@link Activity#onPause()} is called.
   */
  void onPause();

  /**
   * Called when {@link Activity#onResume()} is called.
   */
  void onResume();

  /**
   * Called when {@link Activity#onDestroy()} is called.
   */
  void onDestroy();
}
