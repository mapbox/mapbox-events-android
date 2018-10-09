package com.mapbox.android.core.location;

import android.location.LocationListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractLocationEngineImplTest {
  @Mock
  private LocationEngineCallback<LocationEngineResult> callback;

  @Mock
  private LocationListener locationListener;

  @Spy
  private AbstractLocationEngineImpl spyEngine;

  @Test
  public void testListenerForNull() {
    when(spyEngine.createListener(callback)).thenReturn(locationListener);
    assertThat(spyEngine.mapLocationListener(callback)).isNotNull();
  }

  @Test
  public void testAddRemoveListener() {
    when(spyEngine.createListener(callback)).thenReturn(locationListener);
    LocationListener addedlocationListener = (LocationListener) spyEngine.mapLocationListener(callback);
    assertThat(locationListener).isSameAs(addedlocationListener);

    LocationListener removedLocationListener = (LocationListener) spyEngine.unmapLocationListener(callback);
    assertThat(locationListener).isSameAs(removedLocationListener);
  }

  @Test
  public void testAddListenerTwice() {
    spyEngine.mapLocationListener(callback);
    spyEngine.mapLocationListener(callback);
    assertThat(spyEngine.registeredListeners()).isEqualTo(1);
  }

  @Test
  public void testAddTwoListeners() {
    spyEngine.mapLocationListener(callback);
    LocationEngineCallback<LocationEngineResult> anotherCallback = mock(LocationEngineCallback.class);
    spyEngine.mapLocationListener(anotherCallback);
    assertThat(spyEngine.registeredListeners()).isEqualTo(2);
  }

  @Test
  public void testRemoveUnaddedListener() {
    assertThat(spyEngine.unmapLocationListener(callback)).isNull();
  }
}