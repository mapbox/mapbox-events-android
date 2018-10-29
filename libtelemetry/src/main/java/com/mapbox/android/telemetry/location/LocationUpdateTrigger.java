package com.mapbox.android.telemetry.location;

import android.os.Handler;
import android.os.Looper;

class LocationUpdateTrigger implements Timer {
  private final Handler handler = new Handler(Looper.myLooper());
  private final Callback callback;

  private final Runnable onTimerExpired = new Runnable() {
    @Override
    public void run() {
      callback.onExpired();
    }
  };

  LocationUpdateTrigger(Callback callback) {
    this.callback = callback;
  }

  @Override
  public void start(long timeout) {
    handler.postDelayed(onTimerExpired, timeout);
  }

  @Override
  public void cancel() {
    handler.removeCallbacks(onTimerExpired);
  }
}
