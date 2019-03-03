package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.content.Intent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@RunWith(MockitoJUnitRunner.class)
public class LocationUpdatesBroadcastReceiverTest {
  private LocationUpdatesBroadcastReceiver broadcastReceiver;

  @Before
  public void setUp() {
    broadcastReceiver = new LocationUpdatesBroadcastReceiver();
  }

  @After
  public void tearDown() {
    broadcastReceiver = null;
  }

  @Test
  public void testNullIntentOnReceive() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    broadcastReceiver.onReceive(mockedContext, null);
    verify(mockedContext, never()).getApplicationContext();
  }

  @Test
  public void testOnReceiveActionReturnNull() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getAction()).thenReturn(null);
    broadcastReceiver.onReceive(mockedContext, mockedIntent);
    verify(mockedIntent, never()).getStringExtra(anyString());
  }
}