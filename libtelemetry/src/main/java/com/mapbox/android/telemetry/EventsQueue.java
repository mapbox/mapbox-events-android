package com.mapbox.android.telemetry;

import android.support.annotation.NonNull;

import java.util.List;

class EventsQueue {
  static final int SIZE_LIMIT = 180;
  private final FullQueueCallback callback;
  private final ConcurrentQueue<Event> queue;

  EventsQueue(@NonNull FullQueueCallback callback, @NonNull ConcurrentQueue<Event> queue) {
    this.callback = callback;
    this.queue = queue;
  }

  boolean isEmpty() {
    return queue.size() == 0;
  }

  int size() {
    return queue.size();
  }

  boolean push(Event event) {
    synchronized (this) {
      if (queue.size() >= SIZE_LIMIT) {
        callback.onFullQueue(flush());
      }
      return queue.add(event);
    }
  }

  List<Event> flush() {
    return queue.flush();
  }
}
