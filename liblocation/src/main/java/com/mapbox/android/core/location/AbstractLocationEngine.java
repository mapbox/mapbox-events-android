package com.mapbox.android.core.location;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

abstract class AbstractLocationEngine<T> {
  private final Map<WeakReference<LocationEngineCallback<Location>>, T> listeners;

  AbstractLocationEngine() {
    listeners = new WeakHashMap<>();
  }

  @NonNull
  protected abstract T getListener(LocationEngineCallback<Location> callback);

  protected abstract void removeListener(@NonNull T listener);

  @NonNull
  T mapLocationListener(@NonNull LocationEngineCallback<Location> callback) {
    if (callback == null) {
      throw new IllegalArgumentException("Callback can't be null");
    }

    WeakReference<LocationEngineCallback<Location>> weakReference = findWeakReference(callback);
    if (weakReference == null) {
      weakReference = new WeakReference<>(callback);
    } else {
      // Remove listener for existing callback
      removeListener(listeners.get(weakReference));
    }

    T listener = getListener(callback);
    listeners.put(weakReference, listener);
    return listener;
  }

  @Nullable
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
