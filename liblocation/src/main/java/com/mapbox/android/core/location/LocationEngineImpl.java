package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

/**
 * Internal location engine implementation interface.
 *
 * @param <T> location listener object type
 */
interface LocationEngineImpl<T> {
  @NonNull
  T getLocationListener(@NonNull LocationEngineCallback<LocationEngineResult> callback);

  @Nullable
  T removeLocationListener(@NonNull LocationEngineCallback<LocationEngineResult> callback);

  @Nullable
  LocationEngineResult extractResult(Intent intent);

  void getLastLocation(@NonNull LocationEngineCallback<LocationEngineResult> callback) throws SecurityException;

  void requestLocationUpdates(@NonNull LocationEngineRequest request,
                              @NonNull T listener, @Nullable Looper looper) throws SecurityException;

  void requestLocationUpdates(@NonNull LocationEngineRequest request,
                              @NonNull PendingIntent pendingIntent) throws SecurityException;

  void removeLocationUpdates(T listener);

  void removeLocationUpdates(PendingIntent pendingIntent);

  @VisibleForTesting
  int getListenersCount();
}
