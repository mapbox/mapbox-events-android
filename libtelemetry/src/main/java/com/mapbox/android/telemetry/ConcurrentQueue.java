package com.mapbox.android.telemetry;


import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class ConcurrentQueue<T> {
  private final Queue<T> queue;
  private int count = 0;

  ConcurrentQueue() {
    this.queue = new ConcurrentLinkedQueue<>();
  }

  boolean add(T event) {
    boolean isAdded = queue.add(event);
    count++;
    return isAdded;
  }

  List<T> flush() {
    List<T> queuedEvents = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      T event = queue.remove();
      queuedEvents.add(event);
    }
    count = 0;
    return queuedEvents;
  }

  boolean enqueue(T event) {
    queue.remove();
    return queue.add(event);
  }

  int size() {
    return count;
  }

  // For testing only
  Queue<T> obtainQueue() {
    return queue;
  }
}
