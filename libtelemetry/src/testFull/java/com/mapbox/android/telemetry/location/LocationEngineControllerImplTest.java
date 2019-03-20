package com.mapbox.android.telemetry.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import com.mapbox.android.core.location.LocationEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LocationEngineControllerImplTest {

  @Mock
  private LocationEngine locationEngine;

  @Mock
  private LocationUpdatesBroadcastReceiver broadcastReceiver;

  private LocationEngineControllerImpl locationEngineController;

  @Test
  public void testOnResume() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine, broadcastReceiver);
    locationEngineController.onResume();
    verify(mockedContext).registerReceiver(any(BroadcastReceiver.class), any(IntentFilter.class));
  }

  @Test
  public void testOnResumePermissionsDisabled() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_DENIED);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine, broadcastReceiver);
    locationEngineController.onResume();
    verifyZeroInteractions(locationEngine);
  }

  @Test
  public void testOnDestroy() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine, broadcastReceiver);
    locationEngineController.onDestroy();
    verify(mockedContext).unregisterReceiver(any(BroadcastReceiver.class));
  }
}