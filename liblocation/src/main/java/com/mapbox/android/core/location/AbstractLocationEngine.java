package com.mapbox.android.core.location;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

abstract class AbstractLocationEngine<T> {
  private final Map<WeakReference<LocationEngineCallback<Location>>, T> listeners;

  AbstractLocationEngine() {
    listeners = new WeakHashMap<>();
  }

  @NonNull
  protected abstract T getListener(LocationEngineCallback<Location> callback);

  @NonNull
  T addLocationListener(LocationEngineCallback<Location> callback) {
    WeakReference<LocationEngineCallback<Location>> wListener = new WeakReference<>(callback);
    T listener = getListener(callback);
    listeners.put(wListener, listener);
    return listener;
  }

  @Nullable
  T removeLocationListener(LocationEngineCallback<Location> callback) {
    WeakReference<LocationEngineCallback<Location>> wListener = findListener(callback);
    if (wListener == null) {
      return null;
    }

    T locationListener = listeners.get(wListener);
    listeners.remove(locationListener);
    return locationListener;
  }

  @VisibleForTesting
  int registeredListeners() {
    return listeners.size();
  }

  @Nullable
  private WeakReference<LocationEngineCallback<Location>> findListener(LocationEngineCallback<Location> callback) {
    Iterator<WeakReference<LocationEngineCallback<Location>>> iterator = listeners.keySet().iterator();
    while (iterator.hasNext()) {
      final WeakReference<LocationEngineCallback<Location>> wListener = iterator.next();
      if (wListener == null) {
        continue;
      }

      final LocationEngineCallback<Location> curCallback = wListener.get();
      if (curCallback != null && curCallback == callback) {
        return wListener;
      }
    }
    return null;
  }
}
