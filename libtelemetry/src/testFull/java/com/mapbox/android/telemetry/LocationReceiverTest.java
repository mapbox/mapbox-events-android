package com.mapbox.android.telemetry;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import android.os.Parcelable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocationReceiverTest {
  private enum Field { Lat, Lon, Alt; }

  private static final int NUMBER_OF_LOCATIONS = 5;

  private static Location[] invalidLocations = {getInvalidLocation(Field.Lat, Double.NaN),
          getInvalidLocation(Field.Lon, Double.NaN),
          getInvalidLocation(Field.Alt, Double.NaN),
          getInvalidLocation(Float.NaN)};
  private static Location[] positiveInfLocations = {getInvalidLocation(Field.Lat, Double.POSITIVE_INFINITY),
          getInvalidLocation(Field.Lon, Double.POSITIVE_INFINITY),
          getInvalidLocation(Field.Alt, Double.POSITIVE_INFINITY),
          getInvalidLocation(Float.POSITIVE_INFINITY)};
  private static Location[] negativeInfLocations = {getInvalidLocation(Field.Lat, Double.NEGATIVE_INFINITY),
          getInvalidLocation(Field.Lon, Double.NEGATIVE_INFINITY),
          getInvalidLocation(Field.Alt, Double.NEGATIVE_INFINITY),
          getInvalidLocation(Float.NEGATIVE_INFINITY)};

  @Test
  public void checksSendEventCalled() {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);

    ArrayList<Parcelable> mockedLocations = getLocations(NUMBER_OF_LOCATIONS);
    when(mockedIntent.getParcelableArrayListExtra(eq(LocationManager.KEY_LOCATION_CHANGED)))
            .thenReturn(mockedLocations);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);
    verify(mockedEventCallback, times(NUMBER_OF_LOCATIONS)).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenLocationNaN() {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");

    MapboxTelemetry.applicationContext = mockedContext;
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);

    ArrayList<Parcelable> mockedLocations = getInvalidLocations(invalidLocations);
    when(mockedIntent.getParcelableArrayListExtra(eq(LocationManager.KEY_LOCATION_CHANGED)))
            .thenReturn(mockedLocations);

    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);
    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenLocationPositiveInfinite() {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");

    MapboxTelemetry.applicationContext = mockedContext;
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);

    ArrayList<Parcelable> mockedLocations = getInvalidLocations(positiveInfLocations);
    when(mockedIntent.getParcelableArrayListExtra(eq(LocationManager.KEY_LOCATION_CHANGED)))
            .thenReturn(mockedLocations);

    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);
    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenLocationNegativeInfinite() {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");

    MapboxTelemetry.applicationContext = mockedContext;
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);

    ArrayList<Parcelable> mockedLocations = getInvalidLocations(negativeInfLocations);
    when(mockedIntent.getParcelableArrayListExtra(eq(LocationManager.KEY_LOCATION_CHANGED)))
            .thenReturn(mockedLocations);

    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);
    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  private static Location getInvalidLocation(Field index, double value) {
    Location location = mock(Location.class);
    switch (index) {
      case Lat:
        when(location.getLatitude()).thenReturn(value);
        break;
      case Lon:
        when(location.getLongitude()).thenReturn(value);
        break;
      case Alt:
        when(location.getAltitude()).thenReturn(value);
        break;
      default:
        break;
    }
    return location;
  }

  private static Location getInvalidLocation(float value) {
    Location location = mock(Location.class);
    when(location.getAccuracy()).thenReturn(value);
    return location;
  }

  private static ArrayList<Parcelable> getInvalidLocations(Location[] locations) {
    ArrayList<Parcelable> locationList = new ArrayList<>();
    Collections.addAll(locationList, locations);
    return locationList;
  }

  private static ArrayList<Parcelable> getLocations(int mocksCount) {
    ArrayList<Parcelable> locations = new ArrayList<>();
    for (int i = 0; i < mocksCount; i++) {
      locations.add(mock(Location.class));
    }
    return locations;
  }
}