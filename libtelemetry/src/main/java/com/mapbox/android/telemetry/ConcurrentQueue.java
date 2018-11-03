package com.mapbox.android.telemetry;


import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class ConcurrentQueue<T> {
  private final Queue<T> queue;

  ConcurrentQueue() {
    this.queue = new ConcurrentLinkedQueue<>();
  }

  boolean add(T event) {
    return queue.add(event);
  }

  List<T> flush() {
    int count = queue.size();
    List<T> queuedEvents = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      T event = queue.poll();
      queuedEvents.add(event);
    }
    return queuedEvents;
  }

  boolean enqueue(T event) {
    queue.poll();
    return queue.add(event);
  }

  int size() {
    return queue.size();
  }

  // For testing only
  Queue<T> obtainQueue() {
    return queue;
  }
}
