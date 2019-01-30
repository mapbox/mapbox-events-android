package com.mapbox.android.telemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import static com.mapbox.android.telemetry.SchedulerFlusherFactory.SCHEDULER_FLUSHER_INTENT;

class AlarmReceiver extends BroadcastReceiver {
  private final SchedulerCallback callback;

  AlarmReceiver(@NonNull SchedulerCallback callback) {
    this.callback = callback;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (SCHEDULER_FLUSHER_INTENT.equals(intent.getAction())) {
      callback.onPeriodRaised();
    }
  }

  Intent supplyIntent() {
    return new Intent(SCHEDULER_FLUSHER_INTENT);
  }
}
