package com.mapbox.services.android.telemetry;

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

  @Test
  public void checksOnFullQueueSendEventsCalledWhenIsConnected() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String aValidAccessToken = "validAccessToken";
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))
      .thenReturn(mockedConnectivityManager);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedNetworkInfo.isConnected()).thenReturn(true);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, times(1)).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenConnectivityNotAvailable() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String aValidAccessToken = "validAccessToken";
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))
      .thenReturn(mockedConnectivityManager);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(null);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenIsNotConnected() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String aValidAccessToken = "validAccessToken";
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))
      .thenReturn(mockedConnectivityManager);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedNetworkInfo.isConnected()).thenReturn(false);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnEventReceivedPushCalled() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    Event mockedEvent = mock(Event.class);

    theMapboxTelemetry.onEventReceived(mockedEvent);

    verify(mockedEventsQueue, times(1)).push(eq(mockedEvent));
  }

  @Test
  public void checksPush() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    Event mockedEvent = mock(Event.class);

    theMapboxTelemetry.push(mockedEvent);

    verify(mockedEventsQueue, times(1)).push(eq(mockedEvent));
  }

  @Test
  public void checksEnabled() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);

    boolean isEnabled = theMapboxTelemetry.enable();

    assertTrue(isEnabled);
  }

  @Test
  public void checksStartServiceWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);

    theMapboxTelemetry.enable();

    verify(mockedContext, times(1)).startService(eq(theMapboxTelemetry.obtainLocationServiceIntent()));
  }

  @Test
  public void checksRegisterReceiverWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    EventReceiver theEventReceiver = theMapboxTelemetry.obtainEventReceiver();
    IntentFilter theEventReceiverIntentFilter = theMapboxTelemetry.obtainEventReceiverIntentFilter();

    theMapboxTelemetry.enable();

    verify(mockedLocalBroadcastManager, times(1)).registerReceiver(eq(theEventReceiver),
      eq(theEventReceiverIntentFilter));
  }

  @Test
  public void checksFlusherRegisteringWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);

    theMapboxTelemetry.enable();

    verify(mockedSchedulerFlusher, times(1)).register();
  }

  @Test
  public void checksFlusherSchedulingWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);

    theMapboxTelemetry.enable();

    verify(mockedSchedulerFlusher, times(1)).schedule(anyLong());
  }

  @Test
  public void checksDisabled() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);

    boolean isEnabled = theMapboxTelemetry.disable();

    assertFalse(isEnabled);
  }

  @Test
  public void checksStopServiceWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.disable();

    verify(mockedContext, times(1)).stopService(eq(theMapboxTelemetry.obtainLocationServiceIntent()));
  }

  @Test
  public void checksUnregisterReceiverWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    EventReceiver theEventReceiver = theMapboxTelemetry.obtainEventReceiver();
    theMapboxTelemetry.enable();

    theMapboxTelemetry.disable();

    verify(mockedLocalBroadcastManager, times(1)).unregisterReceiver(eq(theEventReceiver));
  }

  @Test
  public void checksFlusherUnregisteringWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.disable();

    verify(mockedSchedulerFlusher, times(1)).unregister();
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenNullTelemetryClient() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String nullAccessToken = null;
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))
      .thenReturn(mockedConnectivityManager);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedNetworkInfo.isConnected()).thenReturn(true);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, nullAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenEmptyTelemetryClient() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String emptyValidAccessToken = "";
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))
      .thenReturn(mockedConnectivityManager);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedNetworkInfo.isConnected()).thenReturn(true);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, emptyValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOptedIn() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    boolean isOpted = theMapboxTelemetry.optIn();

    assertTrue(isOpted);
  }

  @Test
  public void checksStartServiceWhenOptedIn() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();
    theMapboxTelemetry.optOut();

    theMapboxTelemetry.optIn();

    verify(mockedContext, times(2)).startService(eq(theMapboxTelemetry.obtainLocationServiceIntent()));
  }

  @Test
  public void checksRegisterReceiverWhenOptedIn() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
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
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);

    boolean isOpted = theMapboxTelemetry.optOut();

    assertFalse(isOpted);
  }

  @Test
  public void checksStopServiceWhenOptedOut() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.optOut();

    verify(mockedContext, times(1)).stopService(eq(theMapboxTelemetry.obtainLocationServiceIntent()));
  }

  @Test
  public void checksUnregisterReceiverWhenOptedOut() throws Exception {
    Context mockedContext = mock(Context.class);
    String aValidAccessToken = "validAccessToken";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, mockedEventsQueue,
      mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock, mockedLocalBroadcastManager);
    EventReceiver theEventReceiver = theMapboxTelemetry.obtainEventReceiver();
    theMapboxTelemetry.enable();

    theMapboxTelemetry.optOut();

    verify(mockedLocalBroadcastManager, times(1)).unregisterReceiver(eq(theEventReceiver));
  }
}