package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.content.pm.PackageManager;
import com.mapbox.android.core.location.LocationEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyInt;

@RunWith(MockitoJUnitRunner.class)
public class LocationEngineControllerImplTest {

  @Mock
  private LocationEngine locationEngine;

  private LocationEngineControllerImpl locationEngineController;

  @Test
  public void testOnResume() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    SessionIdentifier mockedSessionIdentifier = mock(SessionIdentifier.class);
    when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine,
      mockedSessionIdentifier);
    locationEngineController.onResume();
    verify(mockedSessionIdentifier, times(1)).getSessionId();
  }

  @Test
  public void testOnResumePermissionsDisabled() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    SessionIdentifier mockedSessionIdentifier = mock(SessionIdentifier.class);
    when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_DENIED);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine,
      mockedSessionIdentifier);
    locationEngineController.onResume();
    verify(mockedSessionIdentifier, never()).getSessionId();
  }

  @Test
  public void testOnDestroy() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    SessionIdentifier mockedSessionIdentifier = mock(SessionIdentifier.class);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine,
      mockedSessionIdentifier);
    locationEngineController.onDestroy();
    verify(mockedSessionIdentifier, times(1)).getSessionId();
  }

  @Test
  public void checkSessionIdentifier() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine,
      new SessionIdentifier(1000L));
    locationEngineController.setSessionIdentifier(new SessionIdentifier(2000L));
    assertEquals(2000L, locationEngineController.getSessionRotationInterval());
  }
}