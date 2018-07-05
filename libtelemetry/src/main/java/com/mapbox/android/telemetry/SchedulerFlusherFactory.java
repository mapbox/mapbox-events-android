package com.mapbox.android.telemetry;


import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

class SchedulerFlusherFactory {
  static final String SCHEDULER_FLUSHER_INTENT = "com.mapbox.scheduler_flusher";
  private static final String KEY_META_DATA_WAKE_UP = "com.mapbox.AdjustWakeUp";
  static long FLUSHING_PERIOD_IN_MILLIS = 180 * 1000;
  private final Context context;
  private final AlarmReceiver alarmReceiver;

  SchedulerFlusherFactory(Context context, AlarmReceiver alarmReceiver) {
    this.context = context;
    this.alarmReceiver = alarmReceiver;
    checkUpdatePeriod();
  }

  SchedulerFlusher supply() {
    // TODO Remove comment after analyzing the impact on the performance when adding SchedulerFlusherJobService
    // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    // return new JobSchedulerFlusher(context, callback);
    // } else {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    int requestCode = (int) System.currentTimeMillis();
    return new AlarmSchedulerFlusher(context, alarmManager, alarmReceiver, requestCode);
    // }
  }

  private void checkUpdatePeriod() {
    try {
      ApplicationInfo appInformation = context.getPackageManager().getApplicationInfo(context.getPackageName(),
        PackageManager.GET_META_DATA);
      if (appInformation != null && appInformation.metaData != null) {
        boolean adjustWakeUp = appInformation.metaData.getBoolean(KEY_META_DATA_WAKE_UP, false);
        Log.e("test", "adjustWakeUp: " + adjustWakeUp);
        if (adjustWakeUp) {
          FLUSHING_PERIOD_IN_MILLIS = 600000;
        }
      }
    } catch (PackageManager.NameNotFoundException exception) {
      exception.printStackTrace();
    }
  }
}
