package com.mapbox.android.telemetry;


import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class LocationReceiverInstrumentationTest {
  private static final int NUMBER_OF_LOCATIONS = 5;

  @Test
  public void checksLocationIntent() {
    Intent expectedLocationIntent = new Intent("com.mapbox.location_receiver");
    expectedLocationIntent.putExtra("location_received", "onLocation");

    List<Location> locations = getLocations(NUMBER_OF_LOCATIONS);
    Intent locationIntent = LocationReceiver.supplyIntent(locations);

    assertTrue(locationIntent.filterEquals(expectedLocationIntent));
    assertTrue(locationIntent.hasExtra("location_received"));
    assertTrue(locationIntent.getStringExtra("location_received").equals("onLocation"));
    assertTrue(locationIntent.hasExtra(LocationManager.KEY_LOCATION_CHANGED));
    assertEquals(locations, locationIntent.getParcelableArrayListExtra(LocationManager.KEY_LOCATION_CHANGED));
  }

  private static List<Location> getLocations(int mocksCount) {
    List<Location> locations = new ArrayList<>();
    for (int i = 0; i < mocksCount; i++) {
      locations.add(mock(Location.class));
    }
    return locations;
  }
}
