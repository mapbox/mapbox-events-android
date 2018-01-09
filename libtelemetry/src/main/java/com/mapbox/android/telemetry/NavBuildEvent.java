package com.mapbox.android.telemetry;

interface NavBuildEvent {

  Event build(NavigationState navigationState);
}
