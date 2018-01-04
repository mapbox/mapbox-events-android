package com.mapbox.services.android.core.location;


import com.google.android.gms.location.LocationRequest;

interface UpdateGoogleRequestPriority {
  void update(LocationRequest request);
}
