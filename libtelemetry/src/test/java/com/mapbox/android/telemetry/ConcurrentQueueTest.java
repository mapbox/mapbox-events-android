package com.mapbox.android.telemetry;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ConcurrentQueueTest {

  @Test
  public void checksAdding() throws Exception {
    ConcurrentQueue<Event> theQueue = new ConcurrentQueue<>();
    Event mockedEvent = mock(Event.class);

    theQueue.add(mockedEvent);

    assertTrue(theQueue.obtainQueue().contains(mockedEvent));
    assertEquals(1, theQueue.size());
  }

  @Test
  public void checksFlushing() throws Exception {
    ConcurrentQueue<Event> theQueue = new ConcurrentQueue<>();
    Event mockedEvent = mock(Event.class);
    List<Event> expectedEventsFlushed = new ArrayList<>(1);
    expectedEventsFlushed.add(mockedEvent);
    theQueue.add(mockedEvent);

    List<Event> eventsFlushed = theQueue.flush();

    assertEquals(expectedEventsFlushed, eventsFlushed);
    assertEquals(0, theQueue.size());
  }

  @Test
  public void checksEmptyFlushing() throws Exception {
    ConcurrentQueue<Event> theQueue = new ConcurrentQueue<>();

    List<Event> eventsFlushed = theQueue.flush();

    assertEquals(0, eventsFlushed.size());
    assertEquals(0, theQueue.size());
  }

  @Test
  public void checksEnqueuing() throws Exception {
    ConcurrentQueue<Event> theQueue = new ConcurrentQueue<>();
    Event firstEvent = mock(Event.class);
    theQueue.add(firstEvent);
    Event secondEvent = mock(Event.class);

    theQueue.enqueue(secondEvent);

    assertFalse(theQueue.obtainQueue().contains(firstEvent));
    assertTrue(theQueue.obtainQueue().contains(secondEvent));
    assertEquals(1, theQueue.size());
  }
}