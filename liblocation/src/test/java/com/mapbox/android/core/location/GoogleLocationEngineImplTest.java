package com.mapbox.android.core.location;

import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public class GoogleLocationEngineImplTest {
  private static final double LATITUDE = 37.7749;
  private static final double LONGITUDE = 122.4194;
  private static final long INTERVAL = 1000L;

  @Mock
  private FusedLocationProviderClient fusedLocationProviderClientMock;

  private LocationEngine engine;

  @Before
  public void setUp() {
    engine = new LocationEngineProxy<>(new GoogleLocationEngineImpl(fusedLocationProviderClientMock));
  }

  @Test(expected = NullPointerException.class)
  public void getLastLocationNullCallback() {
    engine.getLastLocation(null);
  }

  @Test(expected = NullPointerException.class)
  public void requestLocationUpdatesNullCallback() {
    engine.requestLocationUpdates(null, null, null);
  }

  @After
  public void tearDown() {
    reset(fusedLocationProviderClientMock);
    engine = null;
  }

  private static LocationEngineCallback<LocationEngineResult> getCallback(
    final AtomicReference<LocationEngineResult> resultRef,
    final CountDownLatch latch) {
    return new LocationEngineCallback<LocationEngineResult>() {
      @Override
      public void onSuccess(LocationEngineResult result) {
        resultRef.set(result);
        latch.countDown();
      }

      @Override
      public void onFailure(Exception exception) {
        exception.printStackTrace();
      }
    };
  }

  private static LocationEngineResult getMockEngineResult(Location location) {
    return LocationEngineResult.create(location);
  }

  private static Location getMockLocation(double lat, double lon) {
    Location location = mock(Location.class);
    location.setLatitude(lat);
    location.setLongitude(lon);
    return location;
  }
}
