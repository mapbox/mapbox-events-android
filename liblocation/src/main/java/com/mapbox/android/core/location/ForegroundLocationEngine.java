package com.mapbox.android.core.location;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class ForegroundLocationEngine implements LocationEngine {
  final LocationEngineImpl locationEngineImpl;

  ForegroundLocationEngine(LocationEngineImpl locationEngineImpl) {
    this.locationEngineImpl = locationEngineImpl;
  }

  @Override
  public void getLastLocation(@NonNull LocationEngineCallback<LocationEngineResult> callback) throws SecurityException {
    locationEngineImpl.getLastLocation(callback);
  }

  @Override
  public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                     @NonNull LocationEngineCallback<LocationEngineResult> callback,
                                     @Nullable Looper looper) throws SecurityException {
    locationEngineImpl.requestLocationUpdates(request, locationEngineImpl.getLocationListener(callback),
            looper == null ? Looper.getMainLooper() : looper);
  }

  @Override
  public void removeLocationUpdates(@NonNull LocationEngineCallback<LocationEngineResult> callback) {
    locationEngineImpl.removeLocationUpdates(locationEngineImpl.removeLocationListener(callback));
  }
}
