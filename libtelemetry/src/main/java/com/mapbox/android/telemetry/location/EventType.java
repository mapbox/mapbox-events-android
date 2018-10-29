package com.mapbox.android.telemetry.location;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static com.mapbox.android.telemetry.location.EventType.GeofenceExited;
import static com.mapbox.android.telemetry.location.EventType.LocationUpdated;
import static com.mapbox.android.telemetry.location.EventType.TimerExpired;
import static com.mapbox.android.telemetry.location.EventType.Foreground;
import static com.mapbox.android.telemetry.location.EventType.Background;
import static com.mapbox.android.telemetry.location.EventType.Stopped;

import static java.lang.annotation.RetentionPolicy.SOURCE;

@Retention(SOURCE)
@IntDef( {LocationUpdated, GeofenceExited, TimerExpired, Foreground, Background, Stopped })
@interface EventType {
  int LocationUpdated = 1;
  int GeofenceExited = 2;
  int TimerExpired = 3;
  int Foreground = 4;
  int Background = 5;
  int Stopped = 6;
}