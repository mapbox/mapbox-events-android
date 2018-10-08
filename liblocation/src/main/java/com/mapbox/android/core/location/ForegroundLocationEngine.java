package com.mapbox.android.core.location;

import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class ForegroundLocationEngine implements LocationEngine {
  final LocationEngineImpl locationEngineImpl;

  ForegroundLocationEngine(LocationEngineImpl locationEngineImpl) {
    this.locationEngineImpl = locationEngineImpl;
  }

  @Override
  public void getLastLocation(@NonNull LocationEngineCallback<Location> callback) throws SecurityException {
    locationEngineImpl.getLastLocation(callback);
  }

  @Override
  public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                     @NonNull LocationEngineCallback<Location> callback,
                                     @Nullable Looper looper) throws SecurityException {
    locationEngineImpl.requestLocationUpdates(request, locationEngineImpl.getLocationListener(callback),
            looper);
  }

  @Override
  public void removeLocationUpdates(@NonNull LocationEngineCallback<Location> callback) {
    locationEngineImpl.removeLocationUpdates(locationEngineImpl.removeLocationListener(callback));
  }
}
