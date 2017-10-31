package com.mapbox.services.android.telemetry;


interface FlushQueueCallback {

  void onFullQueueFlush(ConcurrentQueue queue, Event event);
}
