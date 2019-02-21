package com.mapbox.android.telemetry;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;

class EventsQueue {
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

  static synchronized EventsQueue create(@NonNull FullQueueCallback callback) {
    ExecutorService executorService = new ThreadPoolExecutor(0, 1,
      20, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
      threadFactory("EventsFullQueueDispatcher"));
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
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        callback.onFullQueue(events);
      }
    });
  }

  private static ThreadFactory threadFactory(final String name) {
    return new ThreadFactory() {
      @Override
      public Thread newThread(Runnable runnable) {
        return new Thread(runnable, name);
      }
    };
  }
}
