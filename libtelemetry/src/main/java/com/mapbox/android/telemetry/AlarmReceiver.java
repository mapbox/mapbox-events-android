package com.mapbox.android.telemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import static com.mapbox.android.telemetry.SchedulerFlusherFactory.SCHEDULER_FLUSHER_INTENT;

class AlarmReceiver extends BroadcastReceiver {
  static final int REQUEST_CODE = 5328;
  private static final String ALARM_FIRED_INTENT_KEY = "alarm_fired";
  private static final String ON_ALARM_INTENT_EXTRA = "onAlarm";
  private static final String REQUEST_CODE_EXTRA = "requestCode";
  private final SchedulerCallback callback;

  AlarmReceiver(@NonNull SchedulerCallback callback) {
    this.callback = callback;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    String alarm = intent.getStringExtra(ALARM_FIRED_INTENT_KEY);
    int requestCode = intent.getIntExtra(REQUEST_CODE_EXTRA, 0);
    if (ON_ALARM_INTENT_EXTRA.equals(alarm) && requestCode == REQUEST_CODE) {
      callback.onPeriodRaised();
    }
  }

  Intent supplyIntent() {
    Intent alarmIntent = new Intent(SCHEDULER_FLUSHER_INTENT);
    alarmIntent.putExtra(ALARM_FIRED_INTENT_KEY, ON_ALARM_INTENT_EXTRA);
    alarmIntent.putExtra(REQUEST_CODE_EXTRA, REQUEST_CODE);
    return alarmIntent;
  }

  static int getRequestCode() {
    return REQUEST_CODE;
  }
}
