package com.mapbox.android.telemetry.location;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EventDispatcherTest {
  @Mock
  private ExecutorService executorService;

  @Mock
  private LocationEngineController locationEngineController;

  private EventDispatcher eventDispatcher;

  @Before
  public void setUp() {
    eventDispatcher = new EventDispatcher(executorService, 2);
    eventDispatcher.setLocationEngineController(locationEngineController);
  }

  @Test
  public void checkIfEQueueIsFull() {
    eventDispatcher = new EventDispatcher(executorService, 0);
    eventDispatcher.enqueue(EventFactory.createBackgroundEvent());
    verify(executorService, never()).execute(any(Runnable.class));
  }

  @Test
  public void checkIfEnqueueCrashesWithNullController() {
    eventDispatcher.setLocationEngineController(null);
    eventDispatcher.enqueue(EventFactory.createBackgroundEvent());
    verify(executorService, never()).execute(any(Runnable.class));
  }
}
