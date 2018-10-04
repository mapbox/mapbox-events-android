package com.mapbox.android.core.location;

import android.location.Location;
import android.location.LocationListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractLocationEngineTest {
  @Mock
  private LocationEngineCallback<Location> callback;

  @Mock
  private LocationListener locationListener;

  @Spy
  private AbstractLocationEngine spyEngine;

  @Test
  public void testAddRemoveListener() {
    when(spyEngine.getListener(callback)).thenReturn(locationListener);
    spyEngine.addLocationListener(callback);

    LocationListener removedLocationListener = (LocationListener) spyEngine.removeLocationListener(callback);
    assertEquals(locationListener, removedLocationListener);
  }

  @Test
  public void testAddNullListener() {
    when(spyEngine.getListener(null)).thenReturn(locationListener);
    assertNotNull(spyEngine.addLocationListener(null));
  }

  @Test
  public void testRemoveNullListener() {
    when(spyEngine.getListener(null)).thenReturn(locationListener);
    assertNull(spyEngine.removeLocationListener(null));
  }
}