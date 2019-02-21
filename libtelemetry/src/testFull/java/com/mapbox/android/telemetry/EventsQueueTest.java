package com.mapbox.android.telemetry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EventsQueueTest {

  @Mock
  private FullQueueCallback mockedCallback;

  private ConcurrentQueue<Event> queue;
  private EventsQueue eventsQueueWrapper;

  @Before
  public void setUp() {
    queue = new ConcurrentQueue<>();
    eventsQueueWrapper = new EventsQueue(mockedCallback, queue);
  }

  @After
  public void tearDown() {
    eventsQueueWrapper = null;
    queue = null;
    reset(mockedCallback);
  }

  @Test
  public void checksAddingAnEventToTheQueue() {
    Event event = mock(Event.class);
    assertThat(eventsQueueWrapper.push(event)).isTrue();
    assertThat(eventsQueueWrapper.isEmpty()).isFalse();
    assertThat(queue.remove()).isSameAs(event);
  }

  @Test
  public void checksQueueFlushing() {
    fillQueue(EventsQueue.SIZE_LIMIT);
    List<Event> originalQueue = new ArrayList<>(queue.obtainQueue());
    List<Event> actualQueue = eventsQueueWrapper.flush();
    assertThat(originalQueue).containsAll(actualQueue);
    assertThat(eventsQueueWrapper.isEmpty()).isTrue();
  }

  @Test
  public void checksOnFullQueueFlushCalled() {
    fillQueue(EventsQueue.SIZE_LIMIT);
    eventsQueueWrapper.push(mock(Event.class));
    verify(mockedCallback).onFullQueue(any(List.class));
  }

  @Test
  public void checksMultiThreadOnFullQueueFlushCalled() throws InterruptedException {
    int n = 5;
    final CountDownLatch latch = new CountDownLatch(n);
    class TestThread extends Thread {
      @Override
      public void run() {
        for (int i = 0; i <= EventsQueue.SIZE_LIMIT; i++) {
          eventsQueueWrapper.push(mock(Event.class));
        }
        latch.countDown();
      }
    }

    for (int i = 0; i < n; i++) {
      new TestThread().start();
    }
    latch.await();
    verify(mockedCallback, times(n)).onFullQueue(any(List.class));
  }


  @Test
  public void checksPushingTheEventRightAfterReachingFullCapacity() {
    fillQueue(EventsQueue.SIZE_LIMIT);
    Event event = mock(Event.class);
    eventsQueueWrapper.push(event);
    assertThat(queue.size()).isEqualTo(1);
    assertThat(queue.remove()).isSameAs(event);
    assertThat(eventsQueueWrapper.isEmpty()).isTrue();
  }

  private void fillQueue(int max) {
    for (int i = 0; i < max; i++) {
      eventsQueueWrapper.push(mock(Event.class));
    }
  }
}