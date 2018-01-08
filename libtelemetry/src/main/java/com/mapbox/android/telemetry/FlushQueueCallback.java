package com.mapbox.android.telemetry;


interface FlushQueueCallback {

  void onFullQueueFlush(ConcurrentQueue queue, Event event);
}
