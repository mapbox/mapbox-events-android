package com.mapbox.android.telemetry;


import java.util.List;

interface FullQueueCallback {

  void onFullQueue(List<Event> fullQueue);
}
