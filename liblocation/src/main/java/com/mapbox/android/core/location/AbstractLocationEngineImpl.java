package com.mapbox.android.core.location;

import android.location.Location;
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
  private final Map<WeakReference<LocationEngineCallback<Location>>, T> listeners;

  AbstractLocationEngineImpl() {
    listeners = new WeakHashMap<>();
  }

  @NonNull
  abstract T createListener(LocationEngineCallback<Location> callback);

  abstract void destroyListener(@NonNull T listener);

  T mapLocationListener(@NonNull LocationEngineCallback<Location> callback) {
    if (callback == null) {
      throw new IllegalArgumentException("Callback can't be null");
    }

    WeakReference<LocationEngineCallback<Location>> weakReference = findWeakReference(callback);
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

  T unmapLocationListener(@NonNull LocationEngineCallback<Location> callback) {
    WeakReference<LocationEngineCallback<Location>> weakReference = findWeakReference(callback);
    return weakReference != null ? listeners.remove(weakReference) : null;
  }

  @VisibleForTesting
  int registeredListeners() {
    return listeners.size();
  }

  @Nullable
  private WeakReference<LocationEngineCallback<Location>> findWeakReference(LocationEngineCallback<Location> callback) {
    for (final WeakReference<LocationEngineCallback<Location>> weakReference : listeners.keySet()) {
      if (weakReference == null) {
        continue;
      }

      final LocationEngineCallback<Location> curCallback = weakReference.get();
      if (curCallback != null && curCallback == callback) {
        return weakReference;
      }
    }
    return null;
  }
}
