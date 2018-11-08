package com.mapbox.android.telemetry;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class AlarmMangerInstrumentationTest {

  @Test
  public void checksAlarmCancelledProperly() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(2);
    final AtomicReference<Integer> broadcastTrack = new AtomicReference<>();

    Context context = InstrumentationRegistry.getContext();
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    AlarmReceiver alarmReceiver = obtainAlarmReceiver(broadcastTrack, latch);

    AlarmSchedulerFlusher theAlarmSchedulerFlusher = new AlarmSchedulerFlusher(context, alarmManager,
      alarmReceiver);

    long elapsedMockedTime = 2000;
    long elapsedMockedTime2 = 5000;

    theAlarmSchedulerFlusher.register();
    theAlarmSchedulerFlusher.scheduleExact(elapsedMockedTime);

    theAlarmSchedulerFlusher.register();
    theAlarmSchedulerFlusher.scheduleExact(elapsedMockedTime2);

    Assert.assertFalse(latch.await(30, TimeUnit.SECONDS));
    int result = broadcastTrack.get();

    Assert.assertEquals(1, result);
  }

  @Test
  public void checksSupplyAlarmManager() {
    Context context = InstrumentationRegistry.getContext();
    AlarmReceiver mockedAlarmReceiver = mock(AlarmReceiver.class);
    SchedulerFlusherFactory schedulerFlusherFactory = new SchedulerFlusherFactory(context, mockedAlarmReceiver);

    Object alarmSchedulerFlusher = schedulerFlusherFactory.supply();
    assertNotNull(alarmSchedulerFlusher);

    Assert.assertTrue(alarmSchedulerFlusher instanceof AlarmSchedulerFlusher);
  }

  @Test
  public void checksScheduleExact() throws Exception {
    Context mockedContext = InstrumentationRegistry.getContext();
    AlarmManager mockedAlarmManager = mock(AlarmManager.class);
    AlarmReceiver mockedAlarmReceiver = mock(AlarmReceiver.class);
    AlarmSchedulerFlusher theAlarmSchedulerFlusher = new AlarmSchedulerFlusher(mockedContext, mockedAlarmManager,
      mockedAlarmReceiver);

    Assert.assertTrue(theAlarmSchedulerFlusher.scheduleExact(25));
  }

  private static AlarmReceiver obtainAlarmReceiver(final AtomicReference<Integer> broadcastTrack,
                                                   final CountDownLatch latch) {
    return new AlarmReceiver(new SchedulerCallback() {
      @Override
      public void onPeriodRaised() {}

      @Override
      public void onError() {}
    }) {
      @Override
      public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        int count = broadcastTrack.get() == null ? 0 : broadcastTrack.get();
        broadcastTrack.set(count + 1);
        latch.countDown();
      }
    };
  }
}
