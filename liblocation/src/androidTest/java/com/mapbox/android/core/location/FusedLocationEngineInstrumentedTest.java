package com.mapbox.android.core.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class FusedLocationEngineInstrumentedTest {
  private static final long INTERVAL = 1000L;
  private static final String PROVIDER = "test_provider";
  private ArrayList<LocationEngineProxy> engines = new ArrayList<>();
  private LocationManager mockLocationManager;
  private Location location = new Location(PROVIDER);

  @Before
  public void setUp() {
    location.setLatitude(1.0);
    location.setAltitude(2.0);
    Context mockContext = mock(Context.class);
    mockLocationManager = mock(LocationManager.class);
    when(mockContext.getSystemService(anyString())).thenReturn(mockLocationManager);
    List<String> providers = new ArrayList<>();
    providers.add(PROVIDER);
    when(mockLocationManager.getAllProviders()).thenReturn(providers);
    when(mockLocationManager.getBestProvider(any(Criteria.class), anyBoolean()))
      .thenReturn(LocationManager.GPS_PROVIDER);
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        LocationListener listener = (LocationListener) invocation.getArguments()[3];
        listener.onProviderEnabled(PROVIDER);
        listener.onStatusChanged(PROVIDER, LocationProvider.AVAILABLE, null);
        listener.onLocationChanged(location);
        listener.onProviderDisabled(PROVIDER);
        return null;
      }
    }).when(mockLocationManager)
      .requestLocationUpdates(anyString(), anyLong(), anyFloat(), any(LocationListener.class), any(Looper.class));
    engines.add(new LocationEngineProxy<>(new MapboxFusedLocationEngineImpl(mockContext)));
    engines.add(new LocationEngineProxy<>(new AndroidLocationEngineImpl(mockContext)));
  }

  @Test
  public void checkGetLastLocation() {
    for (LocationEngineProxy engineProxy : engines) {
      engineProxy.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
        @Override
        public void onSuccess(LocationEngineResult result) {

        }

        @Override
        public void onFailure(@NonNull Exception exception) {
          assertEquals("Last location unavailable", exception.getLocalizedMessage());
        }
      });

      when(mockLocationManager.getLastKnownLocation(anyString())).thenReturn(location);
      engineProxy.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
        @Override
        public void onSuccess(LocationEngineResult result) {
          List<Location> list = result.getLocations();
          assertEquals(1, list.size());
          assertEquals(1.0, list.get(0).getLatitude(), 0);
          assertEquals(2.0, list.get(0).getAltitude(), 0);
        }

        @Override
        public void onFailure(@NonNull Exception exception) {

        }
      });
    }
  }

  @Test
  public void checkRequestAndRemoveLocationUpdates() {
    LocationEngineCallback<LocationEngineResult> engineCallback = new LocationEngineCallback<LocationEngineResult>() {
      @Override
      public void onSuccess(LocationEngineResult result) {
        List<Location> list = result.getLocations();
        assertEquals(1, list.size());
        assertEquals(1.0, list.get(0).getLatitude(), 0);
        assertEquals(2.0, list.get(0).getAltitude(), 0);
      }

      @Override
      public void onFailure(@NonNull Exception exception) {

      }
    };
    for (LocationEngineProxy engineProxy : engines) {
      engineProxy.requestLocationUpdates(getRequest(INTERVAL, LocationEngineRequest.PRIORITY_HIGH_ACCURACY),
        engineCallback, null);

      assertNotNull(engineProxy.removeListener(engineCallback));
    }

  }

  private static LocationEngineRequest getRequest(long interval, int priority) {
    return new LocationEngineRequest.Builder(interval).setPriority(priority).build();
  }
}
