package com.mapbox.android.telemetry.location;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

class LocationUpdateTrigger implements Timer {
  private static final String TAG = "LocationUpdateTrigger";
  private final Handler handler;
  private Callback callback;

  private final Runnable onTimerExpired = new Runnable() {
    @Override
    public void run() {
      if (callback != null) {
        callback.onExpired();
      } else {
        Log.e(TAG, "Unregistered timer callback");
      }
    }
  };

  LocationUpdateTrigger(Looper looper) {
    this.handler = new Handler(looper);
  }

  @Override
  public void start(long timeout) {
    cancel();
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
