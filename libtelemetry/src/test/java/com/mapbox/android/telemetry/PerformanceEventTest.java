package com.mapbox.android.telemetry;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PerformanceEventTest {
  @Test
  public void checksPerformanceEvent() throws Exception {
    setupMockedContext();
    Event perfEvent = obtainPerformanceEventWithLatency();

    assertTrue(perfEvent instanceof PerformanceEvent);
  }

  @Test
  public void checksPerformanceEventType() throws Exception {
    setupMockedContext();
    Event perfEvent = obtainPerformanceEventWithLatency();

    assertEquals(Event.Type.NO_OP, perfEvent.obtainType());
  }

  private Event obtainPerformanceEventWithLatency() {
    Bundle data = new Bundle();
    data.putString("metrics_name", "metrics_latency");
    data.putLong("latency", 1000);
    return new PerformanceEvent("anySessionId", data);
  }

  private Event obtainPerformanceEventWithFailure() {
    Bundle data = new Bundle();
    data.putString("metrics_name", "metrics_latency");
    data.putInt("failure_type", 5);
    return new PerformanceEvent("anySessionId", data);
  }

  private void setupMockedContext() {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);
  }
}
