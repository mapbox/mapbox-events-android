package com.mapbox.android.core.location;

import android.location.Location;
import android.location.LocationListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractLocationEngineImplTest {
  @Mock
  private LocationEngineCallback<Location> callback;

  @Mock
  private LocationListener locationListener;

  @Spy
  private AbstractLocationEngineImpl spyEngine;

  @Test
  public void testListenerForNull() {
    when(spyEngine.createListener(callback)).thenReturn(locationListener);
    assertNotNull(spyEngine.mapLocationListener(callback));
  }

  @Test
  public void testAddRemoveListener() {
    when(spyEngine.createListener(callback)).thenReturn(locationListener);
    LocationListener addedlocationListener = (LocationListener)spyEngine.mapLocationListener(callback);
    assertEquals(locationListener, addedlocationListener);

    LocationListener removedLocationListener = (LocationListener) spyEngine.unmapLocationListener(callback);
    assertEquals(locationListener, removedLocationListener);
  }

  @Test
  public void testAddListenerTwice() {
    spyEngine.mapLocationListener(callback);
    spyEngine.mapLocationListener(callback);
    assertEquals(spyEngine.registeredListeners(), 1);
  }

  @Test
  public void testAddTwoListeners() {
    spyEngine.mapLocationListener(callback);
    LocationEngineCallback<Location> anotherCallback = mock(LocationEngineCallback.class);
    spyEngine.mapLocationListener(anotherCallback);
    assertEquals(spyEngine.registeredListeners(), 2);
  }

  @Test
  public void testRemoveUnaddedListener() {
    assertNull(spyEngine.unmapLocationListener(callback));
  }
}