package com.mapbox.services.android.telemetry;


import android.os.Parcelable;

public abstract class Event implements Parcelable {

  public enum Type {
    TURNSTILE, MAP_LOAD, MAP_CLICK, MAP_DRAGEND, LOCATION, NAV_DEPART, NAV_ARRIVE, NAV_CANCEL, NAV_REROUTE,
    NAV_FEEDBACK, NAV_FASTER
  }

  abstract Type obtainType();
}