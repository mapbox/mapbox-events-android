package com.mapbox.android.telemetry;


import java.util.List;

class EventsQueue {
  static final int SIZE_LIMIT = 180;
  private final FlushQueueCallback callback;
  final ConcurrentQueue<Event> queue;
  private boolean isTelemetryInitialized = false;

  EventsQueue(FlushQueueCallback callback) {
    this.callback = callback;
    this.queue = new ConcurrentQueue<>();
  }

  boolean push(Event event) {
    if (checkMaximumSize()) {
      if (!isTelemetryInitialized) {
        return enqueue(event);
      }
      callback.onFullQueueFlush(queue, event);
      return false;
    }

    return queue.add(event);
  }

  List<Event> flush() {
    return queue.flush();
  }

  void setTelemetryInitialized(boolean telemetryInitialized) {
    isTelemetryInitialized = telemetryInitialized;
  }

  private boolean enqueue(Event event) {
    return queue.enqueue(event);
  }

  private boolean checkMaximumSize() {
    return queue.size() >= SIZE_LIMIT;
  }
}
