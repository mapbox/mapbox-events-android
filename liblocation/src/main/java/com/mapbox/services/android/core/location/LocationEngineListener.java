package com.mapbox.services.android.core.location;

import android.location.Location;

/**
 * Callback used in LocationEngine
 */

interface LocationEngineListener {

  void onConnected();

  void onLocationChanged(Location location);
}
