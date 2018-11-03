package com.mapbox.android.telemetry;


interface FlushQueueCallback {

  void onFullQueueFlush(ConcurrentQueue<Event> queue, Event event);
}
