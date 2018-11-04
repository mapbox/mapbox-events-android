package com.mapbox.android.telemetry;


import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class EventsQueue {
  static final int SIZE_LIMIT = 180;
  private final FullQueueCallback callback;
  private final Queue<Event> queue;
  private boolean isTelemetryInitialized = false;

  EventsQueue(FullQueueCallback callback) {
    this.callback = callback;
    this.queue = new ConcurrentLinkedQueue<>();
  }

  boolean push(Event event) {
    if (queue.size() >= SIZE_LIMIT) {
      if (!isTelemetryInitialized) {
        return enqueue(event);
      }

      List<Event> fullQueue = flush();
      queue.add(event);
      callback.onFullQueue(fullQueue);
      return false;
    }

    return queue.add(event);
  }

  List<Event> flush() {
    int count = queue.size();
    List<Event> queuedEvents = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      queuedEvents.add(queue.poll());
    }
    queue.clear();
    return queuedEvents;
  }

  void setTelemetryInitialized(boolean telemetryInitialized) {
    isTelemetryInitialized = telemetryInitialized;
  }

  private boolean enqueue(Event event) {
    queue.poll();
    return queue.add(event);
  }

  public int size() {
    return queue.size();
  }

  public Queue<Event> obtainQueue() {
    return queue;
  }
}
