package com.mapbox.android.core.location;

import android.location.Location;

public interface GoogleLocationEngineListener {

  void onLastLocationReceived(Location location);

  void onLastLocationFailure(String message);
}
