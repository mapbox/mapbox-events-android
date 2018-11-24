package com.mapbox.android.core.location;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LocationEngineInstrumentedTest {
  private static final long INTERVAL = 1000L;

  private static LocationEngine[] foregroundLocationEngines = { getAndroidEngine(),
    getGoogleEngine(), getMapboxEngine()};

  @Rule
  public GrantPermissionRule permissionRule =
          GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

  @Test
  public void getLastLocation() throws Exception {
    for (LocationEngine engine : foregroundLocationEngines) {
      final CountDownLatch latch = new CountDownLatch(1);
      final AtomicReference<LocationEngineResult> resultRef = new AtomicReference<>();

      engine.getLastLocation(getCallback(resultRef, latch));
      assertTrue(latch.await(5, SECONDS));

      LocationEngineResult result = resultRef.get();
      assertNotNull(result.getLastLocation());
    }
  }

  @Test
  public void requestAndRemoveLocationUpdates() throws Exception {
    for (LocationEngine engine : foregroundLocationEngines) {
      final CountDownLatch latch = new CountDownLatch(1);
      final AtomicReference<LocationEngineResult> resultRef = new AtomicReference<>();
      LocationEngineCallback<LocationEngineResult> callback = getCallback(resultRef, latch);

      engine.requestLocationUpdates(getRequest(INTERVAL), callback, null);
      assertTrue(latch.await(5, SECONDS));

      LocationEngineResult result = resultRef.get();
      assertNotNull(result.getLastLocation());
      engine.removeLocationUpdates(callback);
    }
  }

  private static LocationEngine getMapboxEngine() {
    Context context = InstrumentationRegistry.getTargetContext();
    return getEngine(new MapboxFusedLocationEngineImpl(context));
  }

  private static LocationEngine getAndroidEngine() {
    Context context = InstrumentationRegistry.getTargetContext();
    return getEngine(new AndroidLocationEngineImpl(context));
  }

  private static LocationEngine getGoogleEngine() {
    Context context = InstrumentationRegistry.getTargetContext();
    return getEngine(new GoogleLocationEngineImpl(context));
  }

  private static LocationEngine getEngine(LocationEngineImpl implementation) {
    return new LocationEngineProxy<>(implementation);
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
}
