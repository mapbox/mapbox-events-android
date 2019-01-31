package com.mapbox.android.telemetry;


import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class TelemetryReceiverInstrumentationTest {

  @Test
  public void checksBackgroundIntent() throws Exception {
    TelemetryCallback mockedTelemetryCallback = mock(TelemetryCallback.class);
    TelemetryReceiver theTelemetryReceiver = new TelemetryReceiver(mockedTelemetryCallback);
    Intent expectedBackgroundIntent = new Intent("com.mapbox.telemetry_receiver");
    expectedBackgroundIntent.putExtra("background_received", "onBackground");

    Intent backgroundIntent = theTelemetryReceiver.supplyBackgroundIntent();

    assertTrue(backgroundIntent.filterEquals(expectedBackgroundIntent));
    assertTrue(backgroundIntent.hasExtra("background_received"));
    assertTrue(backgroundIntent.getStringExtra("background_received").equals("onBackground"));
  }

  @Test
  public void checksForegroundIntent() throws Exception {
    TelemetryCallback mockedTelemetryCallback = mock(TelemetryCallback.class);
    TelemetryReceiver theTelemetryReceiver = new TelemetryReceiver(mockedTelemetryCallback);
    Intent expectedForegroundIntent = new Intent("com.mapbox.telemetry_receiver");
    expectedForegroundIntent.putExtra("foreground_received", "onForeground");

    Intent foregroundIntent = theTelemetryReceiver.supplyForegroundIntent();

    assertTrue(foregroundIntent.filterEquals(expectedForegroundIntent));
    assertTrue(foregroundIntent.hasExtra("foreground_received"));
    assertTrue(foregroundIntent.getStringExtra("foreground_received").equals("onForeground"));
  }
}
