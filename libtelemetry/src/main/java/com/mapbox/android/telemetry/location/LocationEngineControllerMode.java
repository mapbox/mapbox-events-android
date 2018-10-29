package com.mapbox.android.telemetry.location;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.ACTIVE;
import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.ACTIVE_GEOFENCE;
import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.PASSIVE;
import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.PASSIVE_GEOFENCE;
import static com.mapbox.android.telemetry.location.LocationEngineControllerMode.IDLE;

import static java.lang.annotation.RetentionPolicy.SOURCE;

@Retention(SOURCE)
@IntDef( {ACTIVE, ACTIVE_GEOFENCE, PASSIVE, PASSIVE_GEOFENCE, IDLE})
@interface LocationEngineControllerMode {
  int ACTIVE = 0;
  int ACTIVE_GEOFENCE = 1;
  int PASSIVE = 2;
  int PASSIVE_GEOFENCE = 3;
  int IDLE = 4;
}
