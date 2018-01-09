package com.mapbox.android.telemetry;


import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class LocationReceiverInstrumentationTest {

  @Test
  public void checksLocationIntent() throws Exception {
    LocationReceiver theLocationReceiver = new LocationReceiver();
    Intent expectedLocationIntent = new Intent("com.mapbox.location_receiver");
    expectedLocationIntent.putExtra("location_received", "onLocation");
    Location mockedLocation = mock(Location.class);

    Intent locationIntent = theLocationReceiver.supplyIntent(mockedLocation);

    assertTrue(locationIntent.filterEquals(expectedLocationIntent));
    assertTrue(locationIntent.hasExtra("location_received"));
    assertTrue(locationIntent.getStringExtra("location_received").equals("onLocation"));
    assertTrue(locationIntent.hasExtra(LocationManager.KEY_LOCATION_CHANGED));
    assertEquals(mockedLocation, locationIntent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED));
  }
}
