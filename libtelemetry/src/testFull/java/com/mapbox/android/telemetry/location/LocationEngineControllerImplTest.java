package com.mapbox.android.telemetry.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import com.mapbox.android.core.location.LocationEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
  public void testOnResume() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine, broadcastReceiver);
    setFinalStatic(Build.VERSION.class.getField("CODENAME"), "S");
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
  public void testOnDestroy() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    locationEngineController = new LocationEngineControllerImpl(mockedContext, locationEngine, broadcastReceiver);
    setFinalStatic(Build.VERSION.class.getField("CODENAME"), "S");
    locationEngineController.onDestroy();
    verify(mockedContext).unregisterReceiver(any(BroadcastReceiver.class));
  }

  static void setFinalStatic(Field field, Object newValue) throws Exception {
    field.setAccessible(true);
    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    field.set(null, newValue);
  }
}