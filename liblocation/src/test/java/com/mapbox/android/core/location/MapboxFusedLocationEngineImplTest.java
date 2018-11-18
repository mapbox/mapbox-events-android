package com.mapbox.android.core.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public class MapboxFusedLocationEngineImplTest {
  private static final double LATITUDE = 37.7749;
  private static final double LONGITUDE = 122.4194;
  private static final long INTERVAL = 1000L;

  @Mock
  private LocationManager locationManagerMock;

  private LocationEngine engine;

  @Before
  public void setUp() {
    Context context = mock(Context.class);
    when(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManagerMock);
    engine = new LocationEngineProxy<>(new MapboxFusedLocationEngineImpl(context));
  }

  @Test
  public void getLastLocation() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<LocationEngineResult> resultRef = new AtomicReference<>();

    LocationEngineCallback<LocationEngineResult> callback = getCallback(resultRef, latch);
    final Location location = getMockLocation(LATITUDE, LONGITUDE);
    final LocationEngineResult expectedResult = getMockEngineResult(location);

    when(locationManagerMock.getAllProviders()).thenReturn(Arrays.asList("gps", "network"));
    when(locationManagerMock.getLastKnownLocation(anyString())).thenReturn(location);

    engine.getLastLocation(callback);
    assertTrue(latch.await(5, SECONDS));

    LocationEngineResult result = resultRef.get();
    assertThat(result.getLastLocation()).isEqualTo(expectedResult.getLastLocation());
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
    reset(locationManagerMock);
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
