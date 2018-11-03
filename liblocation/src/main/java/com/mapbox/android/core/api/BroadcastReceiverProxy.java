package com.mapbox.android.core.api;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;

/**
 * Manages broadcast receivers.
 * @since 1.1.0
 */
public interface BroadcastReceiverProxy {
  /**
   * Create instance of broadcast receiver.
   *
   * @param intentHandler handles events.
   * @return instance of broadcast receiver.
   * @since 1.1.0
   */
  BroadcastReceiver createReceiver(IntentHandler intentHandler);

  /**
   * Register broadcast receiver.
   *
   * @param receiver reference to broadcast receiver to register.
   */
  void registerReceiver(BroadcastReceiver receiver);

  /**
   * Unregister broadcast receiver.
   *
   * @param receiver reference to broadcast receiver to unregister.
   * @since 1.1.0
   */
  void unregisterReceiver(BroadcastReceiver receiver);

  /**
   * Create pending intent based on class type and action
   *
   * @return pending intent instance
   * @since 1.1.0
   */
  PendingIntent getPendingIntent();
}
