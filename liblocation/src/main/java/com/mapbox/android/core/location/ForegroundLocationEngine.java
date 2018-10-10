package com.mapbox.android.core.location;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.mapbox.android.core.location.Utils.checkNotNull;

class ForegroundLocationEngine implements LocationEngine {
  final LocationEngineImpl locationEngineImpl;

  ForegroundLocationEngine(LocationEngineImpl locationEngineImpl) {
    this.locationEngineImpl = locationEngineImpl;
  }

  @Override
  public void getLastLocation(@NonNull LocationEngineCallback<LocationEngineResult> callback) throws SecurityException {
    checkNotNull(callback, "callback == null");
    locationEngineImpl.getLastLocation(callback);
  }

  @Override
  public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                     @NonNull LocationEngineCallback<LocationEngineResult> callback,
                                     @Nullable Looper looper) throws SecurityException {
    checkNotNull(request, "request == null");
    checkNotNull(callback, "callback == null");

    locationEngineImpl.requestLocationUpdates(request, locationEngineImpl.getLocationListener(callback),
            looper == null ? Looper.getMainLooper() : looper);
  }

  @Override
  public void removeLocationUpdates(@NonNull LocationEngineCallback<LocationEngineResult> callback) {
    checkNotNull(callback, "callback == null");
    locationEngineImpl.removeLocationUpdates(locationEngineImpl.removeLocationListener(callback));
  }
}
