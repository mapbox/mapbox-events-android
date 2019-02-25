package com.mapbox.android.telemetry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ConcurrentQueueTest {
  private ConcurrentQueue<Event> queue;

  @Before
  public void setUp() {
    queue = new ConcurrentQueue<>();
  }

  @After
  public void tearDown() {
    queue = null;
  }

  @Test
  public void addNullValue() {
    assertThat(queue.add(null)).isFalse();
  }

  @Test
  public void checksAdding() {
    Event mockedEvent = mock(Event.class);
    queue.add(mockedEvent);
    assertThat(queue.obtainQueue().contains(mockedEvent)).isTrue();
    assertThat(queue.size()).isEqualTo(1);
  }

  @Test
  public void checksFlushing() {
    Event mockedEvent = mock(Event.class);
    List<Event> expectedEventsFlushed = new ArrayList<>(1);
    expectedEventsFlushed.add(mockedEvent);
    queue.add(mockedEvent);
    List<Event> eventsFlushed = queue.flush();
    assertThat(expectedEventsFlushed).containsAll(eventsFlushed);
    assertThat(queue.size()).isEqualTo(0);
  }

  @Test
  public void checksEmptyFlushing() {
    List<Event> eventsFlushed = queue.flush();
    assertThat(eventsFlushed.isEmpty()).isTrue();
    assertThat(queue.size()).isEqualTo(0);
  }
}