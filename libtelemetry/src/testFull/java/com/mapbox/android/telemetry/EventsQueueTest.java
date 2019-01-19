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
    FlushQueueCallback mockedFlushCallback = mock(FlushQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFlushCallback);
    Event anEvent = mock(Event.class);

    assertTrue(aQueue.push(anEvent));
    assertEquals(1, aQueue.queue.size());
    assertEquals(anEvent, obtainFirst(aQueue));
  }

  @Test
  public void checksMaximumSizeOfTheQueueWhenTelemetryInitialized() throws Exception {
    FlushQueueCallback mockedFlushCallback = mock(FlushQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFlushCallback);
    aQueue.setTelemetryInitialized(true);
    Event anEvent = mock(Event.class);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      aQueue.push(anEvent);
    }

    assertFalse(aQueue.push(anEvent));
  }

  @Test
  public void checksEnqueueWhenTelemetryNotInitialized() throws Exception {
    FlushQueueCallback mockedFlushCallback = mock(FlushQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFlushCallback);
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

    assertFalse(aQueue.queue.obtainQueue().contains(firstEvent));
    assertEquals(secondEvent, obtainFirst(aQueue));
    assertEquals(lastEvent, obtainLast(aQueue));
  }

  @Test
  public void checksQueueFlushing() throws Exception {
    FlushQueueCallback mockedFlushCallback = mock(FlushQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFlushCallback);
    aQueue.setTelemetryInitialized(true);
    Event anEvent = mock(Event.class);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      aQueue.push(anEvent);
    }
    List<Event> originalQueue = new ArrayList<>(aQueue.queue.obtainQueue());

    List<Event> actualQueue = aQueue.flush();

    assertEquals(originalQueue, actualQueue);
    assertEquals(0, aQueue.queue.size());
  }

  @Test
  public void checksOnFullQueueFlushCalled() throws Exception {
    FlushQueueCallback mockedFlushCallback = mock(FlushQueueCallback.class);
    EventsQueue aQueue = new EventsQueue(mockedFlushCallback);
    aQueue.setTelemetryInitialized(true);
    Event anEvent = mock(Event.class);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      aQueue.push(anEvent);
    }
    Event theEventRightAfterReachingFullCapacity = mock(Event.class);

    aQueue.push(theEventRightAfterReachingFullCapacity);

    verify(mockedFlushCallback).onFullQueueFlush(aQueue.queue, theEventRightAfterReachingFullCapacity);
  }

  @Test
  public void checksPushingTheEventRightAfterReachingFullCapacity() throws Exception {
    FullQueueCallback mockedFullCallback = mock(FullQueueCallback.class);
    FullQueueFlusher eventsFlusher = new FullQueueFlusher(mockedFullCallback);
    EventsQueue aQueue = new EventsQueue(eventsFlusher);
    aQueue.setTelemetryInitialized(true);
    Event anEvent = mock(Event.class);
    for (int i = 0; i < EventsQueue.SIZE_LIMIT; i++) {
      aQueue.push(anEvent);
    }
    Event theEventRightAfterReachingFullCapacity = mock(Event.class);

    aQueue.push(theEventRightAfterReachingFullCapacity);

    assertEquals(1, aQueue.queue.size());
    assertEquals(theEventRightAfterReachingFullCapacity, obtainFirst(aQueue));
    assertEquals(0, aQueue.queue.obtainQueue().size());
  }

  private Event obtainFirst(EventsQueue eventsQueue) {
    return eventsQueue.queue.obtainQueue().remove();
  }

  private Event obtainLast(EventsQueue eventsQueue) {
    Event lastEvent = null;
    Queue<Event> queue = eventsQueue.queue.obtainQueue();
    int count = queue.size();
    for (int i = 0; i < count; i++) {
      lastEvent = queue.remove();
    }
    return lastEvent;
  }
}