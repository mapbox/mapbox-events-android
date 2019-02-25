package com.mapbox.android.telemetry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class EventsQueueTest {

  @Mock
  private FullQueueCallback mockedCallback;

  @Mock
  private ExecutorService mockedExcecutor;

  private ConcurrentQueue<Event> queue;
  private EventsQueue eventsQueueWrapper;

  @Before
  public void setUp() {
    queue = new ConcurrentQueue<>();
    setupDirectExecutor(mockedExcecutor);
    eventsQueueWrapper = new EventsQueue(queue, mockedCallback, mockedExcecutor);
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
  public void checkSize() {
    int eventsToPush = 100;
    fillQueue(eventsToPush);
    assertThat(eventsQueueWrapper.size()).isEqualTo(eventsToPush);
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
  public void checksPushingTheEventRightAfterReachingFullCapacity() {
    fillQueue(EventsQueue.SIZE_LIMIT);
    Event event = mock(Event.class);
    eventsQueueWrapper.push(event);
    assertThat(queue.size()).isEqualTo(1);
    assertThat(queue.remove()).isSameAs(event);
    assertThat(eventsQueueWrapper.isEmpty()).isTrue();
  }

  @Test
  public void multiThreadedStressTest() throws InterruptedException {
    int pushingThreads = 10;
    int flushingThreads = 5;
    final CountDownLatch latchPushing = new CountDownLatch(pushingThreads);
    final CountDownLatch latchFlushing = new CountDownLatch(flushingThreads);
    Random eventCount = new Random();
    Random sleepFlushTime = new Random();

    int i;
    for (i = 0; i < pushingThreads; i++) {
      createPushThread(eventsQueueWrapper, eventCount.nextInt(EventsQueue.SIZE_LIMIT), latchPushing).start();
    }

    for (i = 0; i < flushingThreads; i++) {
      createFlushThread(eventsQueueWrapper, sleepFlushTime, latchFlushing).start();
    }

    assertThat(latchPushing.await(5, TimeUnit.SECONDS)).isTrue();
    assertThat(latchFlushing.await(10, TimeUnit.SECONDS)).isTrue();
  }

  @Test
  public void checksMultiThreadOnFullQueueFlushCalled() throws InterruptedException {
    int pushingThreads = 10;
    final CountDownLatch latchPushing = new CountDownLatch(pushingThreads);
    int i;
    for (i = 0; i < pushingThreads; i++) {
      createPushThread(eventsQueueWrapper, EventsQueue.SIZE_LIMIT + 1, latchPushing).start();
    }
    assertThat(latchPushing.await(5, TimeUnit.SECONDS)).isTrue();
    verify(mockedCallback, times(pushingThreads)).onFullQueue(any(List.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFactory() {
    EventsQueue.create(null, null);
  }

  private void fillQueue(int max) {
    for (int i = 0; i < max; i++) {
      eventsQueueWrapper.push(mock(Event.class));
    }
  }

  private static Thread createPushThread(final EventsQueue queue, final int count, final CountDownLatch latch) {
    return new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < count; i++) {
          queue.push(mock(Event.class));
        }
        latch.countDown();
      }
    });
  }

  private static Thread createFlushThread(final EventsQueue queue, final Random random, final CountDownLatch latch) {
    return new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep((long) (random.nextInt(3) * 1000));
        } catch (InterruptedException ex) {
          ex.printStackTrace();
          return;
        }
        queue.flush();
        latch.countDown();
      }
    });
  }

  private void setupDirectExecutor(ExecutorService executor) {
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        ((Runnable) invocation.getArguments()[0]).run();
        return null;
      }
    }).when(executor).execute(any(Runnable.class));
  }
}