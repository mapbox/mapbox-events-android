package com.mapbox.android.core.api;

import android.content.Intent;

/**
 * Intent handler interface
 * @since 1.1.0
 */
public interface IntentHandler {
  /**
   * Handle intent
   *
   * @param intent intent
   * @since 1.1.0
   */
  void handle(Intent intent);
}
