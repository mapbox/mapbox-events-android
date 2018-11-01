package com.mapbox.android.telemetry.location;

import android.os.Handler;
import android.os.Looper;

class LocationUpdateTrigger implements Timer {
  private final Handler handler;
  private Callback callback;

  private final Runnable onTimerExpired = new Runnable() {
    @Override
    public void run() {
      callback.onExpired();
    }
  };

  LocationUpdateTrigger(Looper looper) {
    this.handler = new Handler(looper);
  }

  @Override
  public void start(long timeout) {
    handler.postDelayed(onTimerExpired, timeout);
  }

  @Override
  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  @Override
  public void cancel() {
    handler.removeCallbacks(onTimerExpired);
  }
}
