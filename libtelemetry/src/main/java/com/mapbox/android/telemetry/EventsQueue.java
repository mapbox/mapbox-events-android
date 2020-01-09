package com.mapbox.android.telemetry;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

class EventsQueue {
  private static final String LOG_TAG = "EventsQueue";
  @VisibleForTesting
  static final int SIZE_LIMIT = 180;
  private final FullQueueCallback callback;
  private final ConcurrentQueue<Event> queue;
  private final ExecutorService executorService;

  @VisibleForTesting
  EventsQueue(@NonNull ConcurrentQueue<Event> queue,
              @NonNull FullQueueCallback callback, @NonNull ExecutorService executorService) {
    this.queue = queue;
    this.callback = callback;
    this.executorService = executorService;
  }

  static synchronized EventsQueue create(@NonNull FullQueueCallback callback,
                                         @NonNull ExecutorService executorService) {
    if (callback == null || executorService == null) {
      throw new IllegalArgumentException("Callback or executor can't be null");
    }
    return new EventsQueue(new ConcurrentQueue<Event>(), callback, executorService);
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
        dispatchCallback(queue.flush());
      }
      return queue.add(event);
    }
  }

  List<Event> flush() {
    synchronized (this) {
      return queue.flush();
    }
  }

  private void dispatchCallback(final List<Event> events) {
    try {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          try {
            callback.onFullQueue(events);
          } catch (Throwable throwable) {
            // TODO: log silent crash
            Log.e(LOG_TAG, throwable.toString());
          }
        }
      });
    } catch (RejectedExecutionException rex) {
      Log.e(LOG_TAG, rex.toString());
    }
  }
}
