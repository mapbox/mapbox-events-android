package com.mapbox.android.telemetry;

interface SchedulerFlusher {

  void register();

  void schedule(long elapsedRealTime);

  void unregister();
}
