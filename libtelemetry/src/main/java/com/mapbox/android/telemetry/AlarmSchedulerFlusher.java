package com.mapbox.android.telemetry;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import static com.mapbox.android.telemetry.SchedulerFlusherFactory.flushingPeriod;
import static com.mapbox.android.telemetry.SchedulerFlusherFactory.SCHEDULER_FLUSHER_INTENT;

class AlarmSchedulerFlusher implements SchedulerFlusher {
  private final Context context;
  private final AlarmManager manager;
  private final AlarmReceiver receiver;
  private final int requestCode;
  private PendingIntent pendingIntent;

  AlarmSchedulerFlusher(Context context, AlarmManager manager, AlarmReceiver receiver, int requestCode) {
    this.context = context;
    this.manager = manager;
    this.receiver = receiver;
    this.requestCode = requestCode;
  }

  @Override
  public void register() {
    Intent alarmIntent = receiver.supplyIntent(requestCode);
    pendingIntent = PendingIntent.getBroadcast(context, requestCode, alarmIntent, 0);
    String action = SCHEDULER_FLUSHER_INTENT + Integer.toString(requestCode);
    IntentFilter filter = new IntentFilter(action);
    context.registerReceiver(receiver, filter);
  }

  @Override
  public void schedule(long elapsedRealTime) {
    long firstFlushingInMillis = elapsedRealTime + flushingPeriod;
    manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, firstFlushingInMillis,
      flushingPeriod, pendingIntent);
  }

  @Override
  public void unregister() {
    manager.cancel(pendingIntent);
    try {
      context.unregisterReceiver(receiver);
    } catch (IllegalArgumentException exception) {
      // No op for the cases in which the OS has unexpectedly unregistered the alarm
      // Shouldn't happen but seen crashes in Samsung devices
    }
  }

  PendingIntent obtainPendingIntent() {
    return pendingIntent;
  }
}
