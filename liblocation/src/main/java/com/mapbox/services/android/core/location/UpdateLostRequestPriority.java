package com.mapbox.services.android.core.location;


import com.mapzen.android.lost.api.LocationRequest;

public interface UpdateLostRequestPriority {
  void update(LocationRequest request);
}
