package com.mapbox.android.core.location;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LocationEngineInstrumentedTest {
  private static final long INTERVAL = 1000L;
  private static final long TIMEOUT = 60;

  private static LocationEngine[] foregroundLocationEngines = {
    getGoogleEngine(),
    getMapboxEngine(),
    getAndroidEngine(),
  };

  @Rule
  public GrantPermissionRule permissionRule =
    GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

  @Test
  public void getLastLocation() throws Exception {
    for (LocationEngine engine : foregroundLocationEngines) {
      final CountDownLatch latch = new CountDownLatch(1);
      final AtomicReference<LocationEngineResult> resultRef = new AtomicReference<>();

      engine.getLastLocation(getCallback(resultRef, latch));
      latch.await();

      LocationEngineResult result = resultRef.get();
      assertNotNull(result.getLastLocation());
    }
  }

  @Test
  public void requestAndRemoveGoogleLocationUpdates() throws Exception {
    LocationEngine engine = getGoogleEngine();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<LocationEngineResult> resultRef = new AtomicReference<>();
    LocationEngineCallback<LocationEngineResult> callback = getCallback(resultRef, latch);

    engine.requestLocationUpdates(getRequest(INTERVAL, LocationEngineRequest.PRIORITY_LOW_POWER), callback, null);
    assertTrue(latch.await(TIMEOUT, TimeUnit.SECONDS));

    LocationEngineResult result = resultRef.get();
    assertNotNull(result.getLastLocation());
    engine.removeLocationUpdates(callback);
  }

  @Test
  public void requestAndRemoveAndroidLocationUpdates() throws Exception {
    LocationEngine engine = getAndroidEngine();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<LocationEngineResult> resultRef = new AtomicReference<>();
    LocationEngineCallback<LocationEngineResult> callback = getCallback(resultRef, latch);

    engine.requestLocationUpdates(getRequest(INTERVAL, LocationEngineRequest.PRIORITY_LOW_POWER), callback, null);
    assertTrue(latch.await(TIMEOUT, TimeUnit.SECONDS));

    LocationEngineResult result = resultRef.get();
    assertNotNull(result.getLastLocation());
    engine.removeLocationUpdates(callback);
  }

  @Test
  public void requestAndRemoveMapboxLocationUpdates() throws Exception {
    LocationEngine engine = getMapboxEngine();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<LocationEngineResult> resultRef = new AtomicReference<>();
    LocationEngineCallback<LocationEngineResult> callback = getCallback(resultRef, latch);

    engine.requestLocationUpdates(getRequest(INTERVAL, LocationEngineRequest.PRIORITY_HIGH_ACCURACY), callback, null);
    assertTrue(latch.await(TIMEOUT, TimeUnit.SECONDS));

    LocationEngineResult result = resultRef.get();
    assertNotNull(result.getLastLocation());
    engine.removeLocationUpdates(callback);
  }

  private static LocationEngine getMapboxEngine() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    return getEngine(new MapboxFusedLocationEngineImpl(context));
  }

  private static LocationEngine getAndroidEngine() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    return getEngine(new AndroidLocationEngineImpl(context));
  }

  private static LocationEngine getGoogleEngine() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    return getEngine(new GoogleLocationEngineImpl(context));
  }

  private static LocationEngine getEngine(LocationEngineImpl implementation) {
    return new LocationEngineProxy<>(implementation);
  }

  private static LocationEngineRequest getRequest(long interval, int priority) {
    return new LocationEngineRequest.Builder(interval).setPriority(priority).build();
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
        latch.countDown();
        exception.printStackTrace();
      }
    };
  }
}
