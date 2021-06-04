package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;

import com.mapbox.android.telemetry.MapboxTelemetry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.LOCATION_COLLECTOR_ENABLED;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_SHARED_PREFERENCES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LocationCollectionClientInstrumentedTest {
  private static final long DEFAULT_INTERVAL = 1000L;
  private LocationCollectionClient ref;

  @Before
  public void setUp() {
    ref = LocationCollectionClient.install(InstrumentationRegistry.getInstrumentation().getTargetContext(),
      DEFAULT_INTERVAL);
  }

  @After
  public void tearDown() {
    LocationCollectionClient.uninstall();
  }

  @Test
  public void verifyTelemetry() {
    MapboxTelemetry telemetry = ref.getTelemetry();
    assertNotNull(telemetry);
  }

  @Test
  public void verifySharedPreferences() throws InterruptedException {
    SharedPreferences sharedPreferences =
      InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences(MAPBOX_SHARED_PREFERENCES,
          Context.MODE_PRIVATE);
    assertFalse(sharedPreferences.getBoolean(LOCATION_COLLECTOR_ENABLED, true));
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(LOCATION_COLLECTOR_ENABLED, true);
    editor.commit();
    Thread.sleep(1000);
    assertTrue(ref.isEnabled());
  }

  @Test
  public void verifySessionInternal() {
    assertEquals(DEFAULT_INTERVAL, ref.getSessionRotationInterval());
    ref.setSessionRotationInterval(DEFAULT_INTERVAL * 2);
    assertEquals(DEFAULT_INTERVAL * 2, ref.getSessionRotationInterval());
  }

  @Test
  public void verifySessionId() throws InterruptedException {
    String id = ref.getSessionId();
    assertEquals(id, ref.getSessionId());
    Thread.sleep(1000);
    assertNotEquals(id, ref.getSessionId());
  }

  @Test
  public void verifySingletonInstall() {
    assertEquals(ref, LocationCollectionClient.install(InstrumentationRegistry.getInstrumentation().getTargetContext(),
      DEFAULT_INTERVAL));
  }

  @Test
  public void verifyCollectorUninstalled() {
    LocationCollectionClient.uninstall();
    assertNotEquals(ref,
        LocationCollectionClient.install(InstrumentationRegistry.getInstrumentation().getTargetContext(),
        DEFAULT_INTERVAL));
  }

  @Test
  public void callGetInstanceAfterInstall() {
    assertEquals(ref, LocationCollectionClient.getInstance());
  }

  @Test
  public void verifyEnabledStatus() {
    ref.setEnabled(true);
    assertTrue(ref.isEnabled());
    ref.setEnabled(false);
    assertFalse(ref.isEnabled());
  }
}