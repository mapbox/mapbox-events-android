package com.mapbox.android.core.location;

import android.location.Location;

public interface GpxLocationListener {

  void onLocationUpdate(Location gpxLocation);
}
