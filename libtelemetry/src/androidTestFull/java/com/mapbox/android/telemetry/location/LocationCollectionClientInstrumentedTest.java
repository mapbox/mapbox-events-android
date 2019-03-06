package com.mapbox.android.telemetry.location;

import android.support.test.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class LocationCollectionClientInstrumentedTest {
  private LocationCollectionClient ref;

  @Before
  public void setUp() {
    ref = LocationCollectionClient.install(InstrumentationRegistry.getTargetContext(),
      1000);
  }

  @After
  public void tearDown() {
    LocationCollectionClient.uninstall();
  }

  @Test
  public void verifySingletonInstall() {
    assertEquals(ref, LocationCollectionClient.install(InstrumentationRegistry.getTargetContext(),
      1000));
  }

  @Test
  public void verifyCollectorUninstalled() {
    LocationCollectionClient.uninstall();
    assertNotEquals(ref, LocationCollectionClient.install(InstrumentationRegistry.getTargetContext(),
      1000));
  }

  @Test
  public void callGetInstanceAfterInstall() {
    assertEquals(ref, LocationCollectionClient.getInstance());
  }

  @Test
  public void verifyEnabledStatus() {
    ref.setEnabled(true);
    assertTrue(ref.isEnabled());
  }
}