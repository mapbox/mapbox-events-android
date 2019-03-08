package com.mapbox.android.telemetry.location;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.test.InstrumentationRegistry;

import com.mapbox.android.telemetry.MapboxTelemetry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LocationUpdatesBroadcastReceiverInstrumentedTest {
  private MapboxTelemetry telemetry;
  private Intent intent;
  private Method isQueueEmpty;

  @Before
  public void setUp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    LocationCollectionClient.install(InstrumentationRegistry.getTargetContext(), 1000);
    LocationCollectionClient collectionClient = LocationCollectionClient.getInstance();
    telemetry = collectionClient.getTelemetry();
    Class telemetryReflect = telemetry.getClass();
    isQueueEmpty = telemetryReflect.getDeclaredMethod("isQueueEmpty");
    assertNotNull(isQueueEmpty);
    isQueueEmpty.setAccessible(true);
    assertTrue((Boolean) isQueueEmpty.invoke(telemetry));
    intent = new Intent(InstrumentationRegistry.getTargetContext(), LocationUpdatesBroadcastReceiver.class);
    intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_LOCATION_UPDATED);
  }

  @After
  public void tearDown() {
    LocationCollectionClient.uninstall();
  }

  @Test
  public void verifyEmptyIntent() throws InvocationTargetException, IllegalAccessException {
    InstrumentationRegistry.getTargetContext().sendBroadcast(intent);
    assertTrue((Boolean) isQueueEmpty.invoke(telemetry));
  }

  @Test
  public void verifyIntentWithLocation() throws InterruptedException, InvocationTargetException,
    IllegalAccessException {
    Location location = new Location("test");
    intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
    InstrumentationRegistry.getTargetContext().sendBroadcast(intent);
    Thread.sleep(100);
    assertFalse((Boolean) isQueueEmpty.invoke(telemetry));
  }
}
