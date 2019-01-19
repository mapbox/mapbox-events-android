package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventReceiverTest {

  @Test
  public void checksOnLocationReceivedCall() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("event_received"))).thenReturn("onEvent");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    LocationEvent mockedLocationEvent = mock(LocationEvent.class);
    when(mockedBundle.getParcelable(eq("event"))).thenReturn(mockedLocationEvent);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    EventReceiver theEventReceiver = new EventReceiver(mockedEventCallback);

    theEventReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, times(1)).onEventReceived(any(Event.class));
  }
}