package com.mapbox.android.core.location;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Maintains callbacks mappings location engine callback to implementation specific callbacks.
 *
 * @param <T> listener object type
 */
abstract class AbstractLocationEngineImpl<T> {
  private final Map<WeakReference<LocationEngineCallback<LocationEngineResult>>, T> listeners;

  AbstractLocationEngineImpl() {
    listeners = new WeakHashMap<>();
  }

  @NonNull
  abstract T createListener(LocationEngineCallback<LocationEngineResult> callback);

  abstract void destroyListener(@NonNull T listener);

  T mapLocationListener(@NonNull LocationEngineCallback<LocationEngineResult> callback) {
    if (callback == null) {
      throw new IllegalArgumentException("Callback can't be null");
    }

    WeakReference<LocationEngineCallback<LocationEngineResult>> weakReference = findWeakReference(callback);
    if (weakReference == null) {
      weakReference = new WeakReference<>(callback);
    } else {
      // Remove listener for existing callback
      destroyListener(listeners.get(weakReference));
    }

    T listener = createListener(callback);
    listeners.put(weakReference, listener);
    return listener;
  }

  T unmapLocationListener(@NonNull LocationEngineCallback<LocationEngineResult> callback) {
    WeakReference<LocationEngineCallback<LocationEngineResult>> weakReference = findWeakReference(callback);
    return weakReference != null ? listeners.remove(weakReference) : null;
  }

  @VisibleForTesting
  int registeredListeners() {
    return listeners.size();
  }

  @Nullable
  private WeakReference<LocationEngineCallback<LocationEngineResult>> findWeakReference(
          LocationEngineCallback<LocationEngineResult> callback) {
    for (final WeakReference<LocationEngineCallback<LocationEngineResult>> weakReference : listeners.keySet()) {
      if (weakReference == null) {
        continue;
      }

      final LocationEngineCallback<LocationEngineResult> curCallback = weakReference.get();
      if (curCallback != null && curCallback == callback) {
        return weakReference;
      }
    }
    return null;
  }
}
