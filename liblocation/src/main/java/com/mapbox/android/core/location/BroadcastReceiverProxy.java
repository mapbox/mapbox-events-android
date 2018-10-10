package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;

/**
 * Manages broadcast receivers.
 */
interface BroadcastReceiverProxy {
  /**
   * Create instance of broadcast receiver.
   *
   * @param intentHandler handles events.
   * @return instance of broadcast receiver.
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
   */
  void unregisterReceiver(BroadcastReceiver receiver);

  /**
   * Create pending intent based on class type and action
   *
   * @return pending intent instance
   */
  PendingIntent getPendingIntent();
}
