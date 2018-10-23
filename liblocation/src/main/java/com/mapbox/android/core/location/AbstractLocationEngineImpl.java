package com.mapbox.android.core.location;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains callbacks mappings location engine callback to implementation specific callbacks.
 *
 * @param <T> listener object type
 */
abstract class AbstractLocationEngineImpl<T> {
  private final Map<LocationEngineCallback<LocationEngineResult>, T> listeners;

  AbstractLocationEngineImpl() {
    listeners = new HashMap<>();
  }

  @NonNull
  abstract T createListener(LocationEngineCallback<LocationEngineResult> callback);


  T mapLocationListener(@NonNull LocationEngineCallback<LocationEngineResult> callback) {
    if (callback == null) {
      throw new IllegalArgumentException("Callback can't be null");
    }

    T listener = createListener(callback);
    listeners.put(callback, listener);
    return listener;
  }

  T unmapLocationListener(@NonNull LocationEngineCallback<LocationEngineResult> callback) {
    return listeners.remove(callback);
  }

  @VisibleForTesting
  int registeredListeners() {
    return listeners.size();
  }
}
