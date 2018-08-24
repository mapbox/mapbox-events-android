package com.mapbox.android.core.location;

import android.location.Location;

public interface LastLocationListener {
  void onLastLocationSuccess(Location location);

  void onLastLocationFail(Exception exception);
}
