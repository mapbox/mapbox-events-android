package com.mapbox.android.telemetry;

import android.app.ActivityManager;
import android.content.Context;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocationEventTest {

  @Test
  public void checksLocationEvent() throws Exception {
    setupMockedContext();
    Event aLocationEvent = obtainALocationEvent();

    assertTrue(aLocationEvent instanceof LocationEvent);
  }

  @Test
  public void checksLocationType() throws Exception {
    setupMockedContext();
    Event aLocationEvent = obtainALocationEvent();

    assertEquals(Event.Type.LOCATION, aLocationEvent.obtainType());
  }

  private Event obtainALocationEvent() {
    float aLatitude = 40.416775f;
    float aLongitude = -3.703790f;
    return new LocationEvent("anySessionId", aLatitude, aLongitude, "");
  }

  private void setupMockedContext() {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);
  }
}