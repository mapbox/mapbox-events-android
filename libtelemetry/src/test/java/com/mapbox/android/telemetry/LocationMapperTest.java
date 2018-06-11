package com.mapbox.android.telemetry;

import android.app.ActivityManager;
import android.content.Context;
import android.location.Location;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocationMapperTest {

  @Test
  public void checksLocationEventNameMapping() throws Exception {
    setupMockedContext();
    Location mockedLocation = mock(Location.class);
    LocationMapper obtainLocationEvent = new LocationMapper();

    LocationEvent actualLocationEvent = obtainLocationEvent.from(mockedLocation);

    assertEquals("location", actualLocationEvent.getEvent());
  }

  @Test
  public void checksLocationEventSourceMapping() throws Exception {
    setupMockedContext();
    Location mockedLocation = mock(Location.class);
    LocationMapper obtainLocationEvent = new LocationMapper();

    LocationEvent actualLocationEvent = obtainLocationEvent.from(mockedLocation);

    assertEquals("mapbox", actualLocationEvent.getSource());
  }

  @Test
  public void checksLocationEventLatitudeMapping() throws Exception {
    setupMockedContext();
    Location mockedLocation = mock(Location.class);
    when(mockedLocation.getLatitude()).thenReturn(51.39430732403739);
    LocationMapper obtainLocationEvent = new LocationMapper();

    LocationEvent actualLocationEvent = obtainLocationEvent.from(mockedLocation);

    assertEquals(51.3943073, actualLocationEvent.getLatitude(), 0);
  }

  @Test
  public void checksLocationEventLongitudeMapping() throws Exception {
    setupMockedContext();
    Location mockedLocation = mock(Location.class);
    when(mockedLocation.getLongitude()).thenReturn(-147.73225836990392);
    LocationMapper obtainLocationEvent = new LocationMapper();

    LocationEvent actualLocationEvent = obtainLocationEvent.from(mockedLocation);

    assertEquals(-147.7322583, actualLocationEvent.getLongitude(), 0);
  }

  @Test
  public void checksLocationEventOperatingSystemMapping() throws Exception {
    setupMockedContext();
    Location mockedLocation = mock(Location.class);
    LocationMapper obtainLocationEvent = new LocationMapper();

    LocationEvent actualLocationEvent = obtainLocationEvent.from(mockedLocation);

    assertTrue(actualLocationEvent.getOperatingSystem().startsWith("Android - "));
  }

  @Test
  public void checksLocationEventWithAltitudeMapping() throws Exception {
    setupMockedContext();
    Location mockedLocation = mock(Location.class);
    when(mockedLocation.hasAltitude()).thenReturn(true);
    when(mockedLocation.getAltitude()).thenReturn(23.43);
    LocationMapper obtainLocationEvent = new LocationMapper();

    LocationEvent actualLocationEvent = obtainLocationEvent.from(mockedLocation);

    assertEquals(23.0, actualLocationEvent.getAltitude(), 0);
  }

  @Test
  public void checksLocationEventWithOverMaxLongitudeMapping() throws Exception {
    setupMockedContext();
    Location mockedLocation = mock(Location.class);
    when(mockedLocation.getLongitude()).thenReturn(187.73225836990392);
    LocationMapper obtainLocationEvent = new LocationMapper();

    LocationEvent actualLocationEvent = obtainLocationEvent.from(mockedLocation);

    assertEquals(-172.2677417, actualLocationEvent.getLongitude(), 0);
  }

  @Test
  public void checksLocationEventWithUnderMinLongitudeMapping() throws Exception {
    setupMockedContext();
    Location mockedLocation = mock(Location.class);
    when(mockedLocation.getLongitude()).thenReturn(-187.73225836990392);
    LocationMapper obtainLocationEvent = new LocationMapper();

    LocationEvent actualLocationEvent = obtainLocationEvent.from(mockedLocation);

    assertEquals(172.2677417, actualLocationEvent.getLongitude(), 0);
  }

  private void setupMockedContext() {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);
  }
}