package com.mapbox.services.android.telemetry;


import android.content.Context;

interface MapBuildEvent {
  Event build(Context context, MapState mapState);
}
