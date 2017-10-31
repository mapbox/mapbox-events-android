package com.mapbox.services.android.telemetry;

import android.app.AlarmManager;
import android.content.Context;
import android.content.IntentFilter;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AlarmSchedulerFlusherTest {

  @Test
  public void checksAlarmRegistered() throws Exception {
    Context mockedContext = mock(Context.class);
    AlarmManager mockedAlarmManager = mock(AlarmManager.class);
    AlarmReceiver mockedAlarmReceiver = mock(AlarmReceiver.class);
    AlarmSchedulerFlusher theAlarmSchedulerFlusher = new AlarmSchedulerFlusher(mockedContext, mockedAlarmManager,
      mockedAlarmReceiver);

    theAlarmSchedulerFlusher.register();

    verify(mockedContext, times(1)).registerReceiver(
      eq(mockedAlarmReceiver),
      refEq(new IntentFilter("scheduler_flusher"))
    );
  }

  @Test
  public void checksAlarmScheduled() throws Exception {
    Context mockedContext = mock(Context.class);
    AlarmManager mockedAlarmManager = mock(AlarmManager.class);
    AlarmReceiver mockedAlarmReceiver = mock(AlarmReceiver.class);
    AlarmSchedulerFlusher theAlarmSchedulerFlusher = new AlarmSchedulerFlusher(mockedContext, mockedAlarmManager,
      mockedAlarmReceiver);
    long elapsedMockedTime = 1000;
    long millisInASecond = 1000;

    theAlarmSchedulerFlusher.schedule(elapsedMockedTime);

    verify(mockedAlarmManager, times(1)).setInexactRepeating(
      eq(AlarmManager.ELAPSED_REALTIME),
      eq(elapsedMockedTime + (180 * millisInASecond)),
      eq(180 * millisInASecond),
      eq(theAlarmSchedulerFlusher.obtainPendingIntent())
    );
  }

  @Test
  public void checksAlarmUnregistered() throws Exception {
    Context mockedContext = mock(Context.class);
    AlarmManager mockedAlarmManager = mock(AlarmManager.class);
    AlarmReceiver mockedAlarmReceiver = mock(AlarmReceiver.class);
    AlarmSchedulerFlusher theAlarmSchedulerFlusher = new AlarmSchedulerFlusher(mockedContext, mockedAlarmManager,
      mockedAlarmReceiver);

    theAlarmSchedulerFlusher.unregister();

    verify(mockedAlarmManager, times(1)).cancel(eq(theAlarmSchedulerFlusher.obtainPendingIntent()));
    verify(mockedContext, times(1)).unregisterReceiver(mockedAlarmReceiver);
  }
}