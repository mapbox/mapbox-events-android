package com.mapbox.android.telemetry.location;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.*;

class EventDispatcher {
  private static final int MAX_POOL_SIZE = 1;
  private static final int KEEP_ALIVE_TIME_SEC = 5;

  private final int maxEvents;
  private final ExecutorService executorService;
  private final Deque<Event> eventQueue = new ArrayDeque<>();

  private LocationEngineController locationEngineController;

  EventDispatcher(ExecutorService executorService, int maxEvents) {
    this.executorService = executorService;
    this.maxEvents = maxEvents;
  }

  static synchronized EventDispatcher create(int maxEvents) {
    ExecutorService executorService = new ThreadPoolExecutor(0, MAX_POOL_SIZE, KEEP_ALIVE_TIME_SEC,
      TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(maxEvents), threadFactory("EventDispatcher"));
    return new EventDispatcher(executorService, maxEvents);
  }

  void setLocationEngineController(LocationEngineController locationEngineController) {
    this.locationEngineController = locationEngineController;
  }

  synchronized void enqueue(Event event) {
    if (eventQueue.size() >= maxEvents) {
      return;
    }

    eventQueue.add(event);
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        locationEngineController.handleEvent(eventQueue.poll());
      }
    });
  }

  void onDestroy() {
    executorService.shutdown();
    synchronized (this) {
      eventQueue.clear();
    }
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
