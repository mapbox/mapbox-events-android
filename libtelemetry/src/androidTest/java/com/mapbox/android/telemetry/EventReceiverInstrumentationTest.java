package com.mapbox.android.telemetry;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class EventReceiverInstrumentationTest {

  @Test
  public void checksEventIntent() throws Exception {
    Intent expectedEventIntent = new Intent("com.mapbox.event_receiver");
    expectedEventIntent.putExtra("event_received", "onEvent");
    Event mockedEvent = mock(Event.class);

    Intent eventIntent = EventReceiver.supplyIntent(mockedEvent);

    assertTrue(eventIntent.filterEquals(expectedEventIntent));
    assertTrue(eventIntent.hasExtra("event_received"));
    assertTrue(eventIntent.getStringExtra("event_received").equals("onEvent"));
    assertTrue(eventIntent.hasExtra("event"));
    assertEquals(mockedEvent, eventIntent.getParcelableExtra("event"));
  }
}