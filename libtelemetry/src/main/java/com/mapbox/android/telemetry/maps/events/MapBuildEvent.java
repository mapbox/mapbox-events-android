package com.mapbox.android.telemetry.maps.events;


import com.mapbox.android.telemetry.Event;

interface MapBuildEvent {
  Event build(MapState mapState);
}
