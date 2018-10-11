package com.mapbox.android.core.location;

import android.content.Intent;

/**
 *  Intent handler interface
 *
 */
interface IntentHandler {
  /**
   * Handle intent
   *
   * @param intent intent
   */
  void handle(Intent intent);
}
