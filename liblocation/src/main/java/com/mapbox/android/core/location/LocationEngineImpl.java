package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Internal location engine implementation interface.
 *
 * @param <T> location listener object type
 */
interface LocationEngineImpl<T> {
  @NonNull
  T getLocationListener(@NonNull LocationEngineCallback<Location> callback);

  @Nullable
  T removeLocationListener(@NonNull LocationEngineCallback<Location> callback);

  @Nullable
  Location extractResult(Intent intent);

  void getLastLocation(@NonNull LocationEngineCallback<Location> callback) throws SecurityException;

  void requestLocationUpdates(@NonNull LocationEngineRequest request,
                              @NonNull T listener, @Nullable Looper looper) throws SecurityException;

  void requestLocationUpdates(@NonNull LocationEngineRequest request,
                              @NonNull PendingIntent pendingIntent) throws SecurityException;

  void removeLocationUpdates(T listener);

  void removeLocationUpdates(PendingIntent pendingIntent);
}
