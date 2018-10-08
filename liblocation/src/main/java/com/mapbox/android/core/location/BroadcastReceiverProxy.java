package com.mapbox.android.core.location;

import android.app.PendingIntent;

/**
 * Manages broadcast receivers.
 *
 * @param <T> broadcast receiver type
 */
interface BroadcastReceiverProxy<T> {
  /**
   * Create instance of broadcast receiver.
   *
   * @param intentHandler handles events.
   * @return instance of broadcast receiver.
   */
  T createReceiver(IntentHandler intentHandler);

  /**
   * Register broadcast receiver.
   *
   * @param receiver reference to broadcast receiver to register.
   * @param action   action to register with.
   */
  void registerReceiver(T receiver, String action);

  /**
   * Unregister broadcast receiver.
   *
   * @param receiver reference to broadcast receiver to unregister.
   */
  void unregisterReceiver(T receiver);

  /**
   * Create pending intent based on class type and action
   *
   * @param clazz  broadcast receiver class type
   * @param action action to associate intent with.
   * @return pending intent instance
   */
  PendingIntent getPendingIntent(Class<T> clazz, String action);
}
