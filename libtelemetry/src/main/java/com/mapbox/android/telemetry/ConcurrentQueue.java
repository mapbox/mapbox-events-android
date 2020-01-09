package com.mapbox.android.telemetry;


import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class ConcurrentQueue<T> {
  private static final String TAG = "ConcurrentQueue";
  private final Queue<T> queue;

  ConcurrentQueue() {
    this.queue = new ConcurrentLinkedQueue<>();
  }

  boolean add(T event) {
    try {
      return queue.add(event);
    } catch (Exception exc) {
      Log.e(TAG, exc.toString());
      return false;
    }
  }

  @Nullable
  T remove() {
    return queue.remove();
  }

  List<T> flush() {
    List<T> queuedEvents = new ArrayList<>(queue.size());
    try {
      queuedEvents.addAll(queue);
      queue.clear();
    } catch (Exception exc) {
      Log.e(TAG, exc.toString());
    }
    return queuedEvents;
  }

  int size() {
    return queue.size();
  }

  @VisibleForTesting
  Queue<T> obtainQueue() {
    return queue;
  }
}
