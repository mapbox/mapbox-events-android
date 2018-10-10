package com.mapbox.android.core.location;

import android.location.Location;
import android.os.Looper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.doAnswer;

public class LocationEngineTest {
  private static final double LATITUDE = 37.7749;
  private static final double LONGITUDE = 122.4194;
  private static final long INTERVAL = 1000L;

  @Mock
  private LocationEngineImpl locationEngineImpl;

  private LocationEngine engine;

  @Before
  public void setUp() {
    locationEngineImpl = mock(LocationEngineImpl.class);
    engine = new ForegroundLocationEngine(locationEngineImpl);
  }

  @Test
  public void getLastLocation() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<LocationEngineResult> resultRef = new AtomicReference<>();

    LocationEngineCallback<LocationEngineResult> callback = getCallback(resultRef, latch);
    final LocationEngineResult expectedResult = getResult(LATITUDE, LONGITUDE);

    setupDoAnswer(expectedResult).when(locationEngineImpl).getLastLocation(callback);

    engine.getLastLocation(callback);
    assertTrue(latch.await(5, SECONDS));

    LocationEngineResult result = resultRef.get();
    assertThat(result).isSameAs(expectedResult);
    assertThat(result.getLastLocation()).isEqualTo(expectedResult.getLastLocation());
  }

  @Test
  public void requestLocationUpdates() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<LocationEngineResult> resultRef = new AtomicReference<>();

    LocationEngineCallback<LocationEngineResult> callback = getCallback(resultRef, latch);
    final LocationEngineResult expectedResult = getResult(LATITUDE, LONGITUDE);
    Looper looper = mock(Looper.class);

    setupDoAnswer(expectedResult).when(locationEngineImpl)
            .requestLocationUpdates(getRequest(INTERVAL),
                    locationEngineImpl.getLocationListener(callback), looper);

    engine.requestLocationUpdates(getRequest(INTERVAL), callback, looper);
    assertTrue(latch.await(5, SECONDS));

    LocationEngineResult result = resultRef.get();
    assertThat(result).isSameAs(expectedResult);
    assertThat(result.getLastLocation()).isEqualTo(expectedResult.getLastLocation());
  }

  @After
  public void tearDown() {
    reset(locationEngineImpl);
    engine = null;
  }

  private static Stubber setupDoAnswer(final LocationEngineResult expectedResult) {
    return doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        LocationEngineCallback<LocationEngineResult> callback = invocation.getArgument(0);
        callback.onSuccess(expectedResult);
        return null;
      }
    });
  }

  private static LocationEngineRequest getRequest(long interval) {
    return new LocationEngineRequest.Builder(interval).build();
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

  private static LocationEngineResult getResult(double lat, double lon) {
    Location location = mock(Location.class);
    location.setLatitude(lat);
    location.setLongitude(lon);
    return LocationEngineResult.create(location);
  }
}