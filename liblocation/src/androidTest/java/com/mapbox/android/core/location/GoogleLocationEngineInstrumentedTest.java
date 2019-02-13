package com.mapbox.android.core.location;

import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;

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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class GoogleLocationEngineInstrumentedTest {
  private static final long INTERVAL = 1000L;
  private static final String PROVIDER = "test_provider";
  private LocationEngineProxy engine;
  private Location location = new Location(PROVIDER);
  private List<Location> locationList = new ArrayList<>();
  private FusedLocationProviderClient fusedLocationProviderClient;
  private Task<Location> mockTask;

  @Before
  public void setUp() {
    location.setLatitude(1.0);
    location.setAltitude(2.0);
    locationList.clear();
    locationList.add(location);
    fusedLocationProviderClient = mock(FusedLocationProviderClient.class);
    mockTask = mock(Task.class);
    when(fusedLocationProviderClient.getLastLocation()).thenReturn(mockTask);
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        LocationCallback listener = (LocationCallback) invocation.getArguments()[1];
        listener.onLocationResult(LocationResult.create(locationList));
        return null;
      }
    }).when(fusedLocationProviderClient)
      .requestLocationUpdates(any(LocationRequest.class), any(LocationCallback.class), any(Looper.class));
    engine = new LocationEngineProxy<>(new GoogleLocationEngineImpl(fusedLocationProviderClient));
  }

  @Test
  public void checkGetLastLocation() {
    when(mockTask.addOnSuccessListener(any(OnSuccessListener.class)))
      .thenAnswer(new Answer<Object>() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
          OnSuccessListener listener = (OnSuccessListener) invocation.getArguments()[0];
          listener.onSuccess(location);
          return null;
        }
      });
    engine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
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
    engine.requestLocationUpdates(getRequest(INTERVAL, LocationEngineRequest.PRIORITY_HIGH_ACCURACY),
      engineCallback, null);

    assertNotNull(engine.removeListener(engineCallback));

  }

  private static LocationEngineRequest getRequest(long interval, int priority) {
    return new LocationEngineRequest.Builder(interval).setPriority(priority).build();
  }
}
