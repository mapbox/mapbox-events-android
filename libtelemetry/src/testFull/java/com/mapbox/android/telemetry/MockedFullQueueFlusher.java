package com.mapbox.android.telemetry;


import java.util.ArrayList;
import java.util.List;

class MockedFullQueueFlusher extends FullQueueFlusher {
  List<Event> queuedEvents;
  Event eventRightAfterReachingFullCapacity;

  MockedFullQueueFlusher(FullQueueCallback fullQueueCallback) {
    super(fullQueueCallback);
  }

  @Override
  public void onFullQueueFlush(ConcurrentQueue queue, Event event) {
    queuedEvents = new ArrayList<>(queue.obtainQueue());
    eventRightAfterReachingFullCapacity = event;
    super.onFullQueueFlush(queue, event);
  }
}
