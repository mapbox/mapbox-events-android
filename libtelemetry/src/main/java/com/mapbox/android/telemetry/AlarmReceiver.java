package com.mapbox.android.telemetry;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.mapbox.android.telemetry.SchedulerFlusherFactory.SCHEDULER_FLUSHER_INTENT;

class AlarmReceiver extends BroadcastReceiver {
  private static final String ALARM_FIRED_INTENT_KEY = "alarm_fired";
  private static final String ON_ALARM_INTENT_EXTRA = "onAlarm";
  private final SchedulerCallback callback;

  AlarmReceiver(SchedulerCallback callback) {
    this.callback = callback;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    String alarm = intent.getStringExtra(ALARM_FIRED_INTENT_KEY);
    if (ON_ALARM_INTENT_EXTRA.equals(alarm)) {
      callback.onPeriodRaised();
    }
  }

  Intent supplyIntent() {
    Intent alarmIntent = new Intent(SCHEDULER_FLUSHER_INTENT);
    alarmIntent.putExtra(ALARM_FIRED_INTENT_KEY, ON_ALARM_INTENT_EXTRA);
    return alarmIntent;
  }
}
