package com.mapbox.android.telemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.Log;

import static com.mapbox.android.telemetry.SchedulerFlusherFactory.SCHEDULER_FLUSHER_INTENT;

class AlarmReceiver extends BroadcastReceiver {
  private static final String TAG = "AlarmReceiver";

  private final SchedulerCallback callback;

  AlarmReceiver(@NonNull SchedulerCallback callback) {
    this.callback = callback;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      if (SCHEDULER_FLUSHER_INTENT.equals(intent.getAction())) {
        callback.onPeriodRaised();
      }
    } catch (Throwable throwable) {
      // TODO: log silent crash
      Log.e(TAG, throwable.toString());
    }
  }

  Intent supplyIntent() {
    return new Intent(SCHEDULER_FLUSHER_INTENT);
  }
}
