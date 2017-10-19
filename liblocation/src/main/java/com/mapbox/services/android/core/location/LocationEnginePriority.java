package com.mapbox.services.android.core.location;

/**
 * Same priorities GMS and Lost support:
 * <p>
 * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
 * https://github.com/mapzen/lost/blob/master/lost/src/main/java/com/mapzen/android/lost/api/LocationRequest.java
 */
class LocationEnginePriority {

  static final int NO_POWER = 0;
  static final int LOW_POWER = 1;
  static final int BALANCED_POWER_ACCURACY = 2;
  static final int HIGH_ACCURACY = 3;
}
