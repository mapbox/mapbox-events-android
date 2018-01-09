package com.mapbox.android.telemetry;


import java.util.List;

class FullQueueFlusher implements FlushQueueCallback {
  private final FullQueueCallback fullQueueCallback;

  FullQueueFlusher(FullQueueCallback fullQueueCallback) {
    this.fullQueueCallback = fullQueueCallback;
  }

  @Override
  public void onFullQueueFlush(ConcurrentQueue queue, Event event) {
    List<Event> fullQueue = queue.flush();
    queue.add(event);
    fullQueueCallback.onFullQueue(fullQueue);
  }
}
