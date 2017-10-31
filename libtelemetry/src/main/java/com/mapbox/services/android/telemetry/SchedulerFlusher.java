package com.mapbox.services.android.telemetry;

// TODO Access can be package-private, remove public modifier after removing instances from the test app
public interface SchedulerFlusher {

  void register();

  void schedule(long elapsedRealTime);

  void unregister();
}
