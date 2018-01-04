package com.mapbox.services.android.telemetry;


import android.os.Parcelable;

import java.util.EnumSet;

public abstract class Event implements Parcelable {

  public enum Type {
    TURNSTILE, MAP_LOAD, MAP_CLICK, MAP_DRAGEND, LOCATION, NAV_DEPART, NAV_ARRIVE, NAV_CANCEL, NAV_REROUTE,
    NAV_FEEDBACK, NAV_FASTER_ROUTE
  }

  static EnumSet<Type> mapGestureEventTypes = EnumSet.of(Type.MAP_CLICK, Type.MAP_DRAGEND);
  static EnumSet<Type> navigationEventTypes = EnumSet.of(Type.NAV_DEPART, Type.NAV_ARRIVE, Type.NAV_CANCEL,
    Type.NAV_REROUTE, Type.NAV_FEEDBACK, Type.NAV_FASTER_ROUTE);

  abstract Type obtainType();
}