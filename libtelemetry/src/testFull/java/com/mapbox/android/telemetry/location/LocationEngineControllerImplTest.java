package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.content.pm.PackageManager;
import com.mapbox.android.core.location.LocationEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class LocationEngineControllerImplTest {

  @Mock
  private LocationEngine locationEngine;

  private LocationEngineControllerImpl locationEngineController;

  @Test
  public void testOnResume() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine);
    locationEngineController.onResume();
    // TODO: figure out how to verify, can't mock PendingIntent
  }

  @Test
  public void testOnResumePermissionsDisabled() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_DENIED);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine);
    locationEngineController.onResume();
    verifyZeroInteractions(locationEngine);
  }

  @Test
  public void testOnDestroy() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine);
    locationEngineController.onDestroy();
    // TODO: figure out how to verify, can't mock PendingIntent
  }
}