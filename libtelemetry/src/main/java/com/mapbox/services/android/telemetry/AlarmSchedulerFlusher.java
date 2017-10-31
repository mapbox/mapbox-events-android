package com.mapbox.services.android.telemetry;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import static com.mapbox.services.android.telemetry.SchedulerFlusherFactory.FLUSHING_PERIOD_IN_MILLIS;
import static com.mapbox.services.android.telemetry.SchedulerFlusherFactory.SCHEDULER_FLUSHER_INTENT;

class AlarmSchedulerFlusher implements SchedulerFlusher {
  private static final int SCHEDULER_FLUSHER_ALARM_ID = 0;
  private static final int NO_FLAGS = 0;
  private final Context context;
  private final AlarmManager manager;
  private final AlarmReceiver receiver;
  private PendingIntent pendingIntent;

  AlarmSchedulerFlusher(Context context, AlarmManager manager, AlarmReceiver receiver) {
    this.context = context;
    this.manager = manager;
    this.receiver = receiver;
  }

  @Override
  public void register() {
    Intent alarmIntent = receiver.supplyIntent();
    pendingIntent = PendingIntent.getBroadcast(context, SCHEDULER_FLUSHER_ALARM_ID, alarmIntent, NO_FLAGS);
    context.registerReceiver(receiver, new IntentFilter(SCHEDULER_FLUSHER_INTENT));
  }

  @Override
  public void schedule(long elapsedRealTime) {
    long firstFlushingInMillis = elapsedRealTime + FLUSHING_PERIOD_IN_MILLIS;
    manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, firstFlushingInMillis,
      FLUSHING_PERIOD_IN_MILLIS, pendingIntent);
  }

  @Override
  public void unregister() {
    manager.cancel(pendingIntent);
    context.unregisterReceiver(receiver);
  }

  PendingIntent obtainPendingIntent() {
    return pendingIntent;
  }
}
