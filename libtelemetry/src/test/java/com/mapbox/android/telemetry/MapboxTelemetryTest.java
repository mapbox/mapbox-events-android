package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import okhttp3.Callback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
  public void checksNonNullContextRequired() throws Exception {
    MapboxTelemetry.applicationContext = null;
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";

    new MapboxTelemetry(null, anyAccessToken, anyUserAgent);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksNonNullApplicationContextRequired() throws Exception {
    MapboxTelemetry.applicationContext = null;
    Context nullApplicationContext = mock(Context.class);
    when(nullApplicationContext.getApplicationContext()).thenReturn(null);
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";

    new MapboxTelemetry(nullApplicationContext, anyAccessToken, anyUserAgent);
  }

  @Test
  public void checksOnFullQueueSendEventsCalledWhenIsConnectedAndTelemetryEnabled() throws Exception {
    Context mockedContext = obtainNetworkConnectedMockedContext();
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedTelemetryClient,
      mockedHttpCallback);
    List<Event> mockedList = mock(List.class);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, times(1)).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenConnectivityNotAvailable() throws Exception {
    Context mockedContext = obtainNetworkNotAvailableMockedContext();
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedTelemetryClient,
      mockedHttpCallback);
    List<Event> mockedList = mock(List.class);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenIsNotConnected() throws Exception {
    Context mockedContext = obtainNetworkNotConnectedMockedContext();
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedTelemetryClient,
      mockedHttpCallback);
    List<Event> mockedList = mock(List.class);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnEventReceivedPushCalledWhenTelemetryEnabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedEventsQueue);
    Event mockedEvent = mock(Event.class);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.onEventReceived(mockedEvent);

    verify(mockedEventsQueue, times(1)).push(eq(mockedEvent));
  }

  @Test
  public void checksOnEventReceivedPushNotCalledWhenTelemetryDisabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedEventsQueue);
    Event mockedEvent = mock(Event.class);

    theMapboxTelemetry.onEventReceived(mockedEvent);

    verify(mockedEventsQueue, never()).push(eq(mockedEvent));
  }

  @Test
  public void checksSendEventImmediatelyIfWhitelisted() throws Exception {
    Context mockedContext = obtainNetworkConnectedMockedContext();
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedTelemetryClient,
      mockedHttpCallback);
    Event whitelistedEvent = new AppUserTurnstile("anySdkIdentifier", "anySdkVersion", false);
    ArgumentCaptor<List<Event>> eventsCaptor = ArgumentCaptor.forClass((Class) List.class);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.push(whitelistedEvent);

    verify(mockedTelemetryClient, times(1)).sendEvents(eventsCaptor.capture(), eq(mockedHttpCallback));
    assertEquals(eventsCaptor.getValue().get(0), whitelistedEvent);
  }

  @Test
  public void checksPushIfTelemetryEnabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedEventsQueue);
    Event mockedEvent = mock(Event.class);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.push(mockedEvent);

    verify(mockedEventsQueue, times(1)).push(eq(mockedEvent));
  }

  @Test
  public void checksPushIfTelemetryDisabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedEventsQueue);
    Event mockedEvent = mock(Event.class);

    theMapboxTelemetry.push(mockedEvent);

    verify(mockedEventsQueue, never()).push(eq(mockedEvent));
  }

  @Test
  public void checksStartServiceWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext);

    theMapboxTelemetry.enable();

    verify(mockedContext, times(1)).startService(eq(theMapboxTelemetry.obtainLocationServiceIntent()));
  }

  @Test
  public void checksRegisterReceiverWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedLocalBroadcastManager);
    EventReceiver theEventReceiver = theMapboxTelemetry.obtainEventReceiver();
    IntentFilter theEventReceiverIntentFilter = theMapboxTelemetry.obtainEventReceiverIntentFilter();

    theMapboxTelemetry.enable();

    verify(mockedLocalBroadcastManager, times(1)).registerReceiver(eq(theEventReceiver),
      eq(theEventReceiverIntentFilter));
  }

  @Test
  public void checksFlusherRegisteringWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedSchedulerFlusher);

    theMapboxTelemetry.enable();

    verify(mockedSchedulerFlusher, times(1)).register();
  }

  @Test
  public void checksFlusherSchedulingWhenEnabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedSchedulerFlusher);

    theMapboxTelemetry.enable();

    verify(mockedSchedulerFlusher, times(1)).schedule(anyLong());
  }

  @Test
  public void checksFlushEnqueuedEventsWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedEventsQueue);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.disable();

    verify(mockedEventsQueue, times(1)).flush();
  }

  @Test
  public void checksStopServiceWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.disable();

    verify(mockedContext, times(1)).stopService(eq(theMapboxTelemetry.obtainLocationServiceIntent()));
  }

  @Test
  public void checksUnregisterReceiverWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedLocalBroadcastManager);
    EventReceiver theEventReceiver = theMapboxTelemetry.obtainEventReceiver();
    theMapboxTelemetry.enable();

    theMapboxTelemetry.disable();

    verify(mockedLocalBroadcastManager, times(1)).unregisterReceiver(eq(theEventReceiver));
  }

  @Test
  public void checksFlusherUnregisteringWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedSchedulerFlusher);
    theMapboxTelemetry.enable();

    theMapboxTelemetry.disable();

    verify(mockedSchedulerFlusher, times(1)).unregister();
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenNullTelemetryClient() throws Exception {
    Context mockedContext = obtainNetworkConnectedMockedContext();
    MapboxTelemetry.applicationContext = mockedContext;
    String nullAccessToken = null;
    String nullUserAgent = null;
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    telemetryEnabler.injectTelemetryState(TelemetryEnabler.State.ENABLED);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, nullAccessToken,
      nullUserAgent, mockedTelemetryClient, mockedHttpCallback);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenEmptyTelemetryClient() throws Exception {
    Context mockedContext = obtainNetworkConnectedMockedContext();
    String emptyValidAccessToken = "";
    String emptyUserAgent = "";
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    telemetryEnabler.injectTelemetryState(TelemetryEnabler.State.ENABLED);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, emptyValidAccessToken,
      emptyUserAgent, mockedTelemetryClient, mockedHttpCallback);
    List<Event> mockedList = mock(List.class);

    theMapboxTelemetry.onFullQueue(mockedList);

    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback));
  }

  @Test
  public void checksValidAccessTokenValidUserAgent() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, aValidAccessToken, aValidUserAgent);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken, aValidUserAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksNullAccessToken() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String invalidAccessTokenNull = null;
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, invalidAccessTokenNull,
      aValidUserAgent);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(invalidAccessTokenNull,
      aValidUserAgent);

    assertFalse(validRequiredParameters);
  }

  @Test
  public void checksUserAgentTelemetryAndroid() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String aValidAccessToken = "validAccessToken";
    String theTelemetryAndroidAgent = "MapboxTelemetryAndroid/";
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, aValidAccessToken,
      theTelemetryAndroidAgent);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken,
      theTelemetryAndroidAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksUserAgentUnity() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String aValidAccessToken = "validAccessToken";
    String theUnityAndroidAgent = "MapboxEventsUnityAndroid/";
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, aValidAccessToken,
      theUnityAndroidAgent);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken,
      theUnityAndroidAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksUserAgentNavigation() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String aValidAccessToken = "validAccessToken";
    String theNavigationAndroidAgent = "mapbox-navigation-android/";
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, aValidAccessToken,
      theNavigationAndroidAgent);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken,
      theNavigationAndroidAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksUserAgentNavigationUi() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String aValidAccessToken = "validAccessToken";
    String theNavigationUiAndroidAgent = "mapbox-navigation-ui-android/";
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, aValidAccessToken,
      theNavigationUiAndroidAgent);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken,
      theNavigationUiAndroidAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksUserAgentEvents() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String aValidAccessToken = "validAccessToken";
    String theEventsAndroidAgent = "MapboxEventsAndroid/";
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, aValidAccessToken,
      theEventsAndroidAgent);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken,
      theEventsAndroidAgent);

    assertTrue(validRequiredParameters);
  }

  @Test
  public void checksInvalidUserAgent() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String aValidAccessToken = "validAccessToken";
    String aInvalidUserAgent = "invalidUserAgent";
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, aValidAccessToken, aInvalidUserAgent);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken, aInvalidUserAgent);

    assertFalse(validRequiredParameters);
  }

  @Test
  public void checksNullUserAgent() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aNullUserAgent = null;
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, aValidAccessToken, aNullUserAgent);
    theMapboxTelemetry.enable();

    boolean validRequiredParameters = theMapboxTelemetry.checkRequiredParameters(aValidAccessToken, aNullUserAgent);

    assertFalse(validRequiredParameters);
  }

  @Test
  public void checksAccessTokenUpdated() throws Exception {
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedTelemetryClient);
    String anotherValidAccessToken = "anotherValidAccessToken";

    boolean updatedAccessToken = theMapboxTelemetry.updateAccessToken(anotherValidAccessToken);

    verify(mockedTelemetryClient, times(1)).updateAccessToken(eq(anotherValidAccessToken));
    assertTrue(updatedAccessToken);
  }

  @Test
  public void checksAccessTokenNotUpdatedWhenAccessTokenNull() throws Exception {
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedTelemetryClient);
    String nullAccessToken = null;

    boolean updatedAccessToken = theMapboxTelemetry.updateAccessToken(nullAccessToken);

    assertFalse(updatedAccessToken);
  }

  @Test
  public void checksAccessTokenNotUpdatedWhenAccessTokenEmpty() throws Exception {
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedTelemetryClient);
    String emptyAccessToken = "";

    boolean updatedAccessToken = theMapboxTelemetry.updateAccessToken(emptyAccessToken);

    assertFalse(updatedAccessToken);
  }

  @Test
  public void checksAccessTokenNotUpdatedWhenTelemetryClientNull() throws Exception {
    TelemetryClient nullTelemetryClient = null;
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(nullTelemetryClient);
    String anotherValidAccessToken = "anotherValidAccessToken";

    boolean updatedAccessToken = theMapboxTelemetry.updateAccessToken(anotherValidAccessToken);

    assertFalse(updatedAccessToken);
  }

  @Test
  public void checksSessionIdRotationIntervalUpdated() throws Exception {
    boolean serviceBound = true;
    TelemetryService mockedTelemetryService = mock(TelemetryService.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(serviceBound, mockedTelemetryService);
    SessionInterval aValidSessionInterval = new SessionInterval(12);

    boolean updatedSessionInterval = theMapboxTelemetry.updateSessionIdRotationInterval(aValidSessionInterval);

    verify(mockedTelemetryService, times(1)).updateSessionIdentifier(any(SessionIdentifier.class));
    assertTrue(updatedSessionInterval);
  }

  @Test
  public void checksSessionIdRotationIntervalNotUpdated() throws Exception {
    boolean serviceNotBound = false;
    TelemetryService mockedTelemetryService = mock(TelemetryService.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(serviceNotBound, mockedTelemetryService);
    SessionInterval aValidSessionInterval = new SessionInterval(12);

    boolean notUpdatedSessionInterval = theMapboxTelemetry.updateSessionIdRotationInterval(aValidSessionInterval);

    verify(mockedTelemetryService, never()).updateSessionIdentifier(any(SessionIdentifier.class));
    assertFalse(notUpdatedSessionInterval);
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context) {
    MapboxTelemetry.applicationContext = context;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    boolean indifferentServiceBound = true;
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    MapboxTelemetry mapboxTelemetry = new MapboxTelemetry(context, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager, indifferentServiceBound, telemetryEnabler);
    return mapboxTelemetry;
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(TelemetryClient telemetryClient) {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    boolean indifferentServiceBound = true;
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    return new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, telemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager, indifferentServiceBound, telemetryEnabler);
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, TelemetryClient telemetryClient,
                                                    Callback httpCallback) {
    MapboxTelemetry.applicationContext = context;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    boolean indifferentServiceBound = true;
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    MapboxTelemetry mapboxTelemetry = new MapboxTelemetry(context, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, telemetryClient, httpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager, indifferentServiceBound, telemetryEnabler);
    return mapboxTelemetry;
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, EventsQueue eventsQueue) {
    MapboxTelemetry.applicationContext = context;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    boolean indifferentServiceBound = true;
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    MapboxTelemetry mapboxTelemetry = new MapboxTelemetry(context, aValidAccessToken, aValidUserAgent,
      eventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager, indifferentServiceBound, telemetryEnabler);
    return mapboxTelemetry;
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, SchedulerFlusher schedulerFlusher) {
    MapboxTelemetry.applicationContext = context;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    boolean indifferentServiceBound = true;
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    MapboxTelemetry mapboxTelemetry = new MapboxTelemetry(context, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, schedulerFlusher, mockedClock,
      mockedLocalBroadcastManager, indifferentServiceBound, telemetryEnabler);
    return mapboxTelemetry;
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, LocalBroadcastManager localBroadcastManager) {
    MapboxTelemetry.applicationContext = context;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    boolean indifferentServiceBound = true;
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    MapboxTelemetry mapboxTelemetry = new MapboxTelemetry(context, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      localBroadcastManager, indifferentServiceBound, telemetryEnabler);
    return mapboxTelemetry;
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, String accessToken, String userAgent) {
    MapboxTelemetry.applicationContext = context;
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    boolean indifferentServiceBound = true;
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    MapboxTelemetry mapboxTelemetry = new MapboxTelemetry(context, accessToken, userAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager, indifferentServiceBound, telemetryEnabler);
    return mapboxTelemetry;
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, String accessToken, String userAgent,
                                                    TelemetryClient telemetryClient, Callback httpCallback) {
    MapboxTelemetry.applicationContext = context;
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    LocalBroadcastManager mockedLocalBroadcastManager = mock(LocalBroadcastManager.class);
    boolean indifferentServiceBound = true;
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    MapboxTelemetry mapboxTelemetry = new MapboxTelemetry(context, accessToken, userAgent,
      mockedEventsQueue, telemetryClient, httpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager, indifferentServiceBound, telemetryEnabler);
    return mapboxTelemetry;
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(boolean isServiceBound, TelemetryService telemetryService) {
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
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    MapboxTelemetry theMapboxTelemetry = new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      mockedLocalBroadcastManager, isServiceBound, telemetryEnabler);
    if (isServiceBound) {
      theMapboxTelemetry.injectTelemetryService(telemetryService);
    }
    return theMapboxTelemetry;
  }

  private Context obtainNetworkConnectedMockedContext() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedNetworkInfo.isConnected()).thenReturn(true);
    return mockedContext;
  }

  private Context obtainNetworkNotAvailableMockedContext() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(null);
    return mockedContext;
  }

  private Context obtainNetworkNotConnectedMockedContext() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ConnectivityManager mockedConnectivityManager = mock(ConnectivityManager.class);
    when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
    when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedNetworkInfo.isConnected()).thenReturn(false);
    return mockedContext;
  }
}