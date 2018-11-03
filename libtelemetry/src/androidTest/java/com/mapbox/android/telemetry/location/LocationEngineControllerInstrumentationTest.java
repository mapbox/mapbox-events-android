package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class LocationEngineControllerInstrumentationTest {
  @Rule
  public GrantPermissionRule permissionRule =
    GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

  private LocationEngineController locationEngineController;

  private CountDownLatch latch;
  private AtomicReference<Location> resultRef;

  @Before
  public void setUp() {
    Context context = InstrumentationRegistry.getTargetContext();
    // Initialize location engine
    LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(context, true);

    // Setup sync constructs
    latch = new CountDownLatch(1);
    resultRef = new AtomicReference<>();

    // Init callback
    LocationEngineController.Callback callback = getCallback(resultRef, latch);

    // Setup controller
    locationEngineController = new LocationEngineController(locationEngine, EventDispatcher.create(5),
      new LocationUpdateTrigger(Looper.getMainLooper()), callback);
  }

  @After
  public void tearDown() {
    locationEngineController.onDestroy();
  }

  @Test
  public void enableLocationControllerExpectFix() throws Exception {
    locationEngineController.onResume();
  }

  private static LocationEngineController.Callback getCallback(
    final AtomicReference<Location> resultRef,
    final CountDownLatch latch) {
    return new LocationEngineController.Callback() {
      @Override
      public void onLocationUpdated(Location location) {
        resultRef.set(location);
        latch.countDown();
      }
    };
  }
}
