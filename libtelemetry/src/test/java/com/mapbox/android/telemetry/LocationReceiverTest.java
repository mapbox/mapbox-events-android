package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocationReceiverTest {

  @Test
  public void checksSendEventCalled() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, times(1)).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenLatitudeNaN() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getLatitude()).thenReturn(Double.NaN);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenLongitudeNaN() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getLongitude()).thenReturn(Double.NaN);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenAltitudeNaN() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getAltitude()).thenReturn(Double.NaN);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenAccuracyNaN() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getAccuracy()).thenReturn(Float.NaN);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenPositiveInfiniteLatitude() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getLatitude()).thenReturn(Double.POSITIVE_INFINITY);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenPositiveInfiniteLongitude() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getLongitude()).thenReturn(Double.POSITIVE_INFINITY);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenPositiveInfiniteAltitude() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getAltitude()).thenReturn(Double.POSITIVE_INFINITY);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenPositiveInfiniteAccuracy() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getAccuracy()).thenReturn(Float.POSITIVE_INFINITY);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenNegativeInfiniteLatitude() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getLatitude()).thenReturn(Double.NEGATIVE_INFINITY);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenNegativeInfiniteLongitude() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getLongitude()).thenReturn(Double.NEGATIVE_INFINITY);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenNegativeInfiniteAltitude() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getAltitude()).thenReturn(Double.NEGATIVE_INFINITY);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }

  @Test
  public void checksSendEventNotCalledWhenNegativeInfiniteAccuracy() throws Exception {
    Context mockedContext = mock(Context.class);
    Intent mockedIntent = mock(Intent.class);
    when(mockedIntent.getStringExtra(eq("location_received"))).thenReturn("onLocation");
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedIntent.getExtras()).thenReturn(mockedBundle);
    Location mockedLocation = mock(Location.class);
    when(mockedBundle.get(eq(LocationManager.KEY_LOCATION_CHANGED))).thenReturn(mockedLocation);
    when(mockedLocation.getAccuracy()).thenReturn(Float.NEGATIVE_INFINITY);
    EventCallback mockedEventCallback = mock(EventCallback.class);
    LocationReceiver theLocationReceiver = new LocationReceiver(mockedEventCallback);

    theLocationReceiver.onReceive(mockedContext, mockedIntent);

    verify(mockedEventCallback, never()).onEventReceived(any(LocationEvent.class));
  }
}