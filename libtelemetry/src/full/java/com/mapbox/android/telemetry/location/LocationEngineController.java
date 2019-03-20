package com.mapbox.android.telemetry.location;

import android.app.Activity;

/**
 * Location controller attached to activity lifecycle events
 */
interface LocationEngineController {
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
