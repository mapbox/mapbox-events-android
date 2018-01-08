package com.mapbox.android.telemetry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocationEventTest {

  @Test
  public void checksLocationEvent() throws Exception {
    Event aLocationEvent = obtainALocationEvent();

    assertTrue(aLocationEvent instanceof LocationEvent);
  }

  @Test
  public void checksLocationType() throws Exception {
    Event aLocationEvent = obtainALocationEvent();

    assertEquals(Event.Type.LOCATION, aLocationEvent.obtainType());
  }

  private Event obtainALocationEvent() {
    float aLatitude = 40.416775f;
    float aLongitude = -3.703790f;
    return new LocationEvent("anySessionId", aLatitude, aLongitude);
  }
}