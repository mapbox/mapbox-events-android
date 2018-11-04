package com.mapbox.android.telemetry;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FullQueueFlusherTest {

  @Test
  public void checksQueuedEventsWhenFullCapacityReached() throws Exception {
    FullQueueCallback mockedFullCallback = mock(FullQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFullCallback);
    aQueue.setTelemetryInitialized(true);
    List<Event> expectedQueuedEvents = new ArrayList<>(EventsQueue.SIZE_LIMIT);
    Event anEvent = mock(Event.class);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      expectedQueuedEvents.add(anEvent);
      aQueue.push(anEvent);
    }
    Event eventRightAfterReachingFullCapacity = mock(Event.class);

    aQueue.push(eventRightAfterReachingFullCapacity);
    verify(mockedFullCallback).onFullQueue(expectedQueuedEvents);
  }

  @Test
  public void checksEventRightAfterReachingFullCapacity() throws Exception {
    FullQueueCallback mockedFullCallback = mock(FullQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFullCallback);
    aQueue.setTelemetryInitialized(true);
    Event anEvent = mock(Event.class);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      aQueue.push(anEvent);
    }
    Event expectedEventRightAfterReachingFullCapacity = mock(Event.class);

    aQueue.push(expectedEventRightAfterReachingFullCapacity);

    assertEquals(expectedEventRightAfterReachingFullCapacity, aQueue.obtainQueue().peek());
  }

  @Test
  public void checksOnFullQueueCalled() throws Exception {
    FullQueueCallback mockedFullCallback = mock(FullQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFullCallback);
    aQueue.setTelemetryInitialized(true);
    List<Event> expectedQueuedEvents = new ArrayList<>(EventsQueue.SIZE_LIMIT);
    Event anEvent = mock(Event.class);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      expectedQueuedEvents.add(anEvent);
      aQueue.push(anEvent);
    }

    Event eventRightAfterReachingFullCapacity = mock(Event.class);
    aQueue.push(eventRightAfterReachingFullCapacity);

    verify(mockedFullCallback).onFullQueue(expectedQueuedEvents);
  }
}