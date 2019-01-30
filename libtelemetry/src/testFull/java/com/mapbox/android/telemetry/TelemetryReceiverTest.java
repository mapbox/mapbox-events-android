package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.Intent;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TelemetryReceiverTest {

  @Test
  public void checksOnBackgroundCall() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("background_received"))).thenReturn("onBackground");
    TelemetryCallback mockedTelemetryCallback = mock(TelemetryCallback.class);
    TelemetryReceiver theTelemetryReceiver = new TelemetryReceiver(mockedTelemetryCallback);

    theTelemetryReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedTelemetryCallback, times(1)).onBackground();
  }

  @Test
  public void checksOnForegroundCall() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("foreground_received"))).thenReturn("onForeground");
    TelemetryCallback mockedTelemetryCallback = mock(TelemetryCallback.class);
    TelemetryReceiver theTelemetryReceiver = new TelemetryReceiver(mockedTelemetryCallback);

    theTelemetryReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedTelemetryCallback, times(1)).onForeground();
  }
}