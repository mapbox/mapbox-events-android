package com.mapbox.android.telemetry;

import android.app.AlarmManager;
import android.content.Context;

class SchedulerFlusherFactory {
  static final String SCHEDULER_FLUSHER_INTENT = "com.mapbox.scheduler_flusher";
  static long flushingPeriod = 180 * 1000;
  private final Context context;
  private final AlarmReceiver alarmReceiver;

  SchedulerFlusherFactory(Context context, AlarmReceiver alarmReceiver) {
    this.context = context;
    this.alarmReceiver = alarmReceiver;
    checkUpdatePeriod(context);
  }

  SchedulerFlusher supply() {
    // TODO Remove comment after analyzing the impact on the performance when adding SchedulerFlusherJobService
    // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    // return new JobSchedulerFlusher(context, callback);
    // } else {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    return new AlarmSchedulerFlusher(context, alarmManager, alarmReceiver);
    // }
  }

  private void checkUpdatePeriod(Context context) {
    if (TelemetryUtils.adjustWakeUpMode(context)) {
      flushingPeriod = 600 * 1000;
    }
  }
}
