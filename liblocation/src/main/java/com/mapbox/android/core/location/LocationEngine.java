package com.mapbox.android.core.location;

import android.location.Location;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public interface LocationEngine {
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void getLastLocation(LocationEngineCallback<Location> callback) throws SecurityException;

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void requestLocationUpdates(LocationEngineRequest request, LocationEngineCallback<Location> callback,
                              @Nullable Looper looper) throws SecurityException;

  void removeLocationUpdates(LocationEngineCallback<Location> callback);
}