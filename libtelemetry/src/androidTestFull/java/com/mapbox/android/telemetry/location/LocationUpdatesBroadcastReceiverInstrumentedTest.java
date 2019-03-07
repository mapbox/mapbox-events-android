package com.mapbox.android.telemetry.location;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.test.InstrumentationRegistry;

import com.mapbox.android.telemetry.MapboxTelemetry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocationUpdatesBroadcastReceiverInstrumentedTest {
  LocationCollectionClient collectionClient;
  MapboxTelemetry telemetry;
  Intent intent;

  @Before
  public void setUp() {
    LocationCollectionClient.install(InstrumentationRegistry.getTargetContext(), 1000);
    collectionClient = LocationCollectionClient.getInstance();
    telemetry = collectionClient.getTelemetry();
    assertTrue(telemetry.isQueueEmpty());
    intent = new Intent(InstrumentationRegistry.getTargetContext(), LocationUpdatesBroadcastReceiver.class);
    intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_LOCATION_UPDATED);
  }

  @After
  public void tearDown() {
    LocationCollectionClient.uninstall();
  }

  @Test
  public void verifyEmptyIntent() {
    InstrumentationRegistry.getTargetContext().sendBroadcast(intent);
    assertTrue(telemetry.isQueueEmpty());
  }

  @Test
  public void verifyIntentWithLocation() throws InterruptedException {
    Location location = new Location("test");
    intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
    InstrumentationRegistry.getTargetContext().sendBroadcast(intent);
    assertFalse(telemetry.isQueueEmpty());
  }
}
