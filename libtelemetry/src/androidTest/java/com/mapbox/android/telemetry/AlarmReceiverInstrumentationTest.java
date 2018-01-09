package com.mapbox.android.telemetry;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class AlarmReceiverInstrumentationTest {

  @Test
  public void checksAlarmIntent() throws Exception {
    SchedulerCallback mockedSchedulerCallback = mock(SchedulerCallback.class);
    AlarmReceiver theAlarmReceiver = new AlarmReceiver(mockedSchedulerCallback);
    Intent expectedAlarmIntent = new Intent("com.mapbox.scheduler_flusher");
    expectedAlarmIntent.putExtra("alarm_fired", "onAlarm");

    Intent alarmIntent = theAlarmReceiver.supplyIntent();

    assertTrue(alarmIntent.filterEquals(expectedAlarmIntent));
    assertTrue(alarmIntent.hasExtra("alarm_fired"));
    assertTrue(alarmIntent.getStringExtra("alarm_fired").equals("onAlarm"));
  }
}