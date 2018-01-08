package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.Intent;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AlarmReceiverTest {

  @Test
  public void checksOnPeriodRaisedCall() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("alarm_fired"))).thenReturn("onAlarm");
    SchedulerCallback mockedSchedulerCallback = mock(SchedulerCallback.class);
    AlarmReceiver theAlarmReceiver = new AlarmReceiver(mockedSchedulerCallback);

    theAlarmReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedSchedulerCallback, times(1)).onPeriodRaised();
  }
}