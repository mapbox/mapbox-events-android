package com.mapbox.android.telemetry;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class ForegroundBackoffTest {

  @Test
  public void checksThreadCreated() throws Exception {
    MapboxTelemetry mockedTelemetry = mock(MapboxTelemetry.class);
    ForegroundBackoff foregroundBackoff = new ForegroundBackoff(mockedTelemetry);

    Thread thread = foregroundBackoff.start();

    Assert.assertNotNull(thread);
    Assert.assertEquals(Thread.State.RUNNABLE, thread.getState());
  }
}
