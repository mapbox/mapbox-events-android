package com.mapbox.android.telemetry;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EventsQueueTest {

  @Test
  public void checksAddingAnEventToTheQueue() throws Exception {
    FullQueueCallback mockedFullCallback = mock(FullQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFullCallback);
    Event anEvent = mock(Event.class);

    assertTrue(aQueue.push(anEvent));
    assertEquals(1, aQueue.size());
    assertEquals(anEvent, obtainFirst(aQueue));
  }

  @Test
  public void checksMaximumSizeOfTheQueueWhenTelemetryInitialized() throws Exception {
    FullQueueCallback mockedFullCallback = mock(FullQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFullCallback);
    aQueue.setTelemetryInitialized(true);
    Event anEvent = mock(Event.class);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      aQueue.push(anEvent);
    }

    assertFalse(aQueue.push(anEvent));
  }

  @Test
  public void checksEnqueueWhenTelemetryNotInitialized() throws Exception {
    FullQueueCallback mockedFullCallback = mock(FullQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFullCallback);
    Event firstEvent = mock(Event.class);
    aQueue.push(firstEvent);
    Event secondEvent = mock(Event.class);
    aQueue.push(secondEvent);
    Event anEvent = mock(Event.class);
    for (int i = 2; i < EventsQueue.SIZE_LIMIT; i++) {
      aQueue.push(anEvent);
    }
    Event lastEvent = mock(Event.class);

    aQueue.push(lastEvent);

    assertFalse(aQueue.obtainQueue().contains(firstEvent));
    assertEquals(secondEvent, obtainFirst(aQueue));
    assertEquals(lastEvent, obtainLast(aQueue));
  }

  @Test
  public void checksQueueFlushing() throws Exception {
    FullQueueCallback mockedFullCallback = mock(FullQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFullCallback);
    aQueue.setTelemetryInitialized(true);
    Event anEvent = mock(Event.class);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      aQueue.push(anEvent);
    }
    List<Event> originalQueue = new ArrayList<>(aQueue.obtainQueue());

    List<Event> actualQueue = aQueue.flush();

    assertEquals(originalQueue, actualQueue);
    assertEquals(0, aQueue.size());
  }

  @Test
  public void checksOnFullQueueFlushCalled() throws Exception {
    FullQueueCallback mockedFullCallback = mock(FullQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFullCallback);
    aQueue.setTelemetryInitialized(true);
    Event anEvent = mock(Event.class);
    List<Event> list = new ArrayList<>(EventsQueue.SIZE_LIMIT);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      aQueue.push(anEvent);
      list.add(anEvent);
    }
    Event theEventRightAfterReachingFullCapacity = mock(Event.class);

    aQueue.push(theEventRightAfterReachingFullCapacity);

    verify(mockedFullCallback).onFullQueue(list);
  }

  @Test
  public void checksPushingTheEventRightAfterReachingFullCapacity() throws Exception {
    FullQueueCallback mockedFullCallback = mock(FullQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFullCallback);
    aQueue.setTelemetryInitialized(true);
    Event anEvent = mock(Event.class);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      aQueue.push(anEvent);
    }
    Event theEventRightAfterReachingFullCapacity = mock(Event.class);

    aQueue.push(theEventRightAfterReachingFullCapacity);

    assertEquals(1, aQueue.size());
    assertEquals(theEventRightAfterReachingFullCapacity, obtainFirst(aQueue));
    assertEquals(0, aQueue.obtainQueue().size());
  }

  private Event obtainFirst(EventsQueue eventsQueue) {
    return eventsQueue.obtainQueue().remove();
  }

  private Event obtainLast(EventsQueue eventsQueue) {
    Event lastEvent = null;
    Queue<Event> queue = eventsQueue.obtainQueue();
    int count = queue.size();
    for (int i = 0; i < count; i++) {
      lastEvent = queue.remove();
    }
    return lastEvent;
  }
}