package com.mapbox.android.core.location;


import com.mapzen.android.lost.api.LocationRequest;

interface UpdateLostRequestPriority {
  void update(LocationRequest request);
}
