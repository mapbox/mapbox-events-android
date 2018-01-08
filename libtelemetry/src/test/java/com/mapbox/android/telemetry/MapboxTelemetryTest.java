package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

import org.junit.Test;

import java.util.List;

import okhttp3.Callback;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapboxTelemetryTest {

  @Test(expected = IllegalArgumentException.class)
  public void checksNonNullApplicationContextRequired() throws Exception {
    MapboxTelemetry.applicationContext = null;
    Context nullApplicationContext = mock(Context.class);
    when(nullApplicationContext.getApplicationContext()).thenReturn(null);
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";
    Callback mockedHttpCallback = mock(Callback.class);

    new MapboxTelemetry(nullApplicationContext, anyAccessToken, anyUserAgent, mockedHttpCallback);
  }

  @Test
  public void checksOnFullQueueSendEventsCalledWhenIsConnected() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedNetworkInfo.isConnected()).thenReturn(true);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, times(1)).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenConnectivityNotAvailable() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(null);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenIsNotConnected() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedNetworkInfo.isConnected()).thenReturn(false);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnEventReceivedPushCalled() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    Event mockedEvent = mock(Event.class);

    theMapboxTelemetry.onEventReceived(mockedEvent);

    verify(mockedEventsQueue, times(1)).push(eq(mockedEvent));
  }

  @Test
  public void checksPush() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    Event mockedEvent = mock(Event.class);

    theMapboxTelemetry.push(mockedEvent);

    verify(mockedEventsQueue, times(1)).push(eq(mockedEvent));
  }

  @Test
  public void checksEnabled() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);

    boolean isEnabled = theMapboxTelemetry.enable();

    assertTrue(isEnabled);
  }

  @Test
  public void checksStartServiceWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);

    theMapboxTelemetry.enable();

    verify(mockedContext, times(1)).startService(eq(theMapboxTelemetry.obtainLocationServiceIntent()));
  }

  @Test
  public void checksRegisterReceiverWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    EventReceiver theEventReceiver = theMapboxTelemetry.obtainEventReceiver();
    IntentFilter theEventReceiverIntentFilter = theMapboxTelemetry.obtainEventReceiverIntentFilter();

    theMapboxTelemetry.enable();

    verify(mockedLocalBroadcastManager, times(1)).registerReceiver(eq(theEventReceiver),
      eq(theEventReceiverIntentFilter));
  }

  @Test
  public void checksFlusherRegisteringWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);

    theMapboxTelemetry.enable();

    verify(mockedSchedulerFlusher, times(1)).register();
  }

  @Test
  public void checksFlusherSchedulingWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);

    theMapboxTelemetry.enable();

    verify(mockedSchedulerFlusher, times(1)).schedule(anyLong());
  }

  @Test
  public void checksDisabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);

    boolean isEnabled = theMapboxTelemetry.disable();

    assertFalse(isEnabled);
  }

  @Test
  public void checksStopServiceWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.disable();

    verify(mockedContext, times(1)).stopService(eq(theMapboxTelemetry.obtainLocationServiceIntent()));
  }

  @Test
  public void checksUnregisterReceiverWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    EventReceiver theEventReceiver = theMapboxTelemetry.obtainEventReceiver();
    theMapboxTelemetry.enable();

    theMapboxTelemetry.disable();

    verify(mockedLocalBroadcastManager, times(1)).unregisterReceiver(eq(theEventReceiver));
  }

  @Test
  public void checksFlusherUnregisteringWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.disable();

    verify(mockedSchedulerFlusher, times(1)).unregister();
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenNullTelemetryClient() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedNetworkInfo.isConnected()).thenReturn(true);
    MapboxTelemetry.applicationContext = mockedContext;
    String nullAccessToken = null;
    String nullUserAgent = null;
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, nullAccessToken, nullUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);
    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenEmptyTelemetryClient() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedNetworkInfo.isConnected()).thenReturn(true);
    MapboxTelemetry.applicationContext = mockedContext;
    String emptyValidAccessToken = "";
    String emptyUserAgent = "";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, emptyValidAccessToken, emptyUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);
    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOptedIn() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    boolean isOpted = theMapboxTelemetry.optIn();

    assertTrue(isOpted);
  }

  @Test
  public void checksStartServiceWhenOptedIn() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();
    theMapboxTelemetry.optOut();

    theMapboxTelemetry.optIn();

    verify(mockedContext, times(2)).startService(eq(theMapboxTelemetry.obtainLocationServiceIntent()));
  }

  @Test
  public void checksRegisterReceiverWhenOptedIn() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    EventReceiver theEventReceiver = theMapboxTelemetry.obtainEventReceiver();
    IntentFilter theEventReceiverIntentFilter = theMapboxTelemetry.obtainEventReceiverIntentFilter();
    theMapboxTelemetry.enable();
    theMapboxTelemetry.optOut();

    theMapboxTelemetry.optIn();

    verify(mockedLocalBroadcastManager, times(2)).registerReceiver(eq(theEventReceiver),
      eq(theEventReceiverIntentFilter));
  }

  @Test
  public void checksOptedOut() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);

    boolean isOpted = theMapboxTelemetry.optOut();

    assertFalse(isOpted);
  }

  @Test
  public void checksStopServiceWhenOptedOut() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.optOut();

    verify(mockedContext, times(1)).stopService(eq(theMapboxTelemetry.obtainLocationServiceIntent()));
  }

  @Test
  public void checksUnregisterReceiverWhenOptedOut() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    EventReceiver theEventReceiver = theMapboxTelemetry.obtainEventReceiver();
    theMapboxTelemetry.enable();

    theMapboxTelemetry.optOut();

    verify(mockedLocalBroadcastManager, times(1)).unregisterReceiver(eq(theEventReceiver));
  }

  @Test
  public void checksValidAccessTokenValidUserAgent() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken, aValidUserAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksNullAccessToken() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String invalidAccessTokenNull = null;
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, invalidAccessTokenNull, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(invalidAccessTokenNull,
      aValidUserAgent);

    assertFalse(validRequiredParameters);
  }

  @Test
  public void checksUserAgentTelemetryAndroid() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String theTelemetryAndroidAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, theTelemetryAndroidAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken,
      theTelemetryAndroidAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksUserAgentUnity() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String theUnityAndroidAgent = "MapboxEventsUnityAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, theUnityAndroidAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken,
      theUnityAndroidAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksUserAgentNavigation() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String theNavigationAndroidAgent = "mapbox-navigation-android/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken,
      theNavigationAndroidAgent, mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher,
      mockedClock, mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken,
      theNavigationAndroidAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksUserAgentNavigationUi() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String theNavigationUiAndroidAgent = "mapbox-navigation-ui-android/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken,
      theNavigationUiAndroidAgent, mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher,
      mockedClock, mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken,
      theNavigationUiAndroidAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksUserAgentEvents() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String theEventsAndroidAgent = "MapboxEventsAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, theEventsAndroidAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken,
      theEventsAndroidAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksInvalidUserAgent() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aInvalidUserAgent = "invalidUserAgent";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aInvalidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken, aInvalidUserAgent);

    assertFalse(validRequiredParameters);
  }

  @Test
  public void checksNullUserAgent() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aNullUserAgent = null;
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aNullUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();


    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken, aNullUserAgent);

    assertFalse(validRequiredParameters);
  }
}