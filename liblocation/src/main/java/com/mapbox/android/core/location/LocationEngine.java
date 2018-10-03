package com.mapbox.android.core.location;

import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Generic location engine interface wrapper for the location providers.
 * Default providers bundled with Mapbox location library:
 * Android location provider and Google Play Services fused location provider
 *
 * @since 3.0.0
 */
public interface LocationEngine {

  /**
   * Returns the most recent location currently available.
   *
   * If a location is not available, which should happen very rarely, null will be returned.
   *
   * @param callback {@link LocationEngineCallback<Location>} for the location updates.
   * @throws SecurityException if permission is not granted to access location services.
   * @since 3.0.0
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void getLastLocation(@NonNull LocationEngineCallback<Location> callback) throws SecurityException;

  /**
   * Requests location updates with a callback on the specified Looper thread.
   *
   * @param request {@link LocationEngineRequest} for the updates.
   * @param callback {@link LocationEngineCallback<Location>} for the location updates.
   * @param looper The Looper object whose message queue will be used to implement the callback mechanism,
   *               or null to invoke callbacks on the main thread.
   * @throws SecurityException if permission is not granted to access location services.
   * @since 3.0.0
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void requestLocationUpdates(@NonNull LocationEngineRequest request,
                              @NonNull LocationEngineCallback<Location> callback,
                              @Nullable Looper looper) throws SecurityException;

  /**
   * Removes location updates for the given location engine callback.
   *
   * @param callback {@link LocationEngineCallback<Location>} to remove.
   * @since 3.0.0
   */
  void removeLocationUpdates(@NonNull LocationEngineCallback<Location> callback);
}