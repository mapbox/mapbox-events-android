package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import okhttp3.Callback;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.DEFAULT_STAGING_EVENTS_HOST;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_SHARED_PREFERENCES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;

public class MapboxTelemetryTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

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
    verify(mockedTelemetryClient, times(1))
      .sendEvents(eq(mockedList), eq(mockedHttpCallback), anyBoolean());
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
    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback), anyBoolean());
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
    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback), anyBoolean());
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
    verify(mockedTelemetryClient, times(1))
      .sendEvents(eventsCaptor.capture(), eq(mockedHttpCallback), anyBoolean());
    assertEquals(eventsCaptor.getValue().get(0), whitelistedEvent);
  }

  @Test
  public void checksPushIfTelemetryEnabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedEventsQueue,
      TelemetryEnabler.State.ENABLED);
    // Mock event that will end up in the queue
    Event mockedEvent = mock(Event.class);
    when(mockedEvent.obtainType()).thenReturn(Event.Type.LOCATION);
    theMapboxTelemetry.push(mockedEvent);
    verify(mockedEventsQueue, times(1)).push(eq(mockedEvent));
  }

  @Test
  public void checksPushIfTelemetryDisabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedEventsQueue,
      TelemetryEnabler.State.DISABLED);
    Event mockedEvent = mock(Event.class);
    when(mockedEvent.obtainType()).thenReturn(Event.Type.LOCATION);
    theMapboxTelemetry.push(mockedEvent);
    verify(mockedEventsQueue, never()).push(eq(mockedEvent));
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
    theMapboxTelemetry.disable();
    verify(mockedEventsQueue, times(1)).flush();
  }

  @Test
  public void checksFlusherUnregisteringWhenDisabled() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedSchedulerFlusher);
    theMapboxTelemetry.disable();
    verify(mockedSchedulerFlusher, times(1)).unregister();
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenNullTelemetryClient() throws Exception {
    Context mockedContext = obtainNetworkConnectedMockedContext();
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, null,
      null, mockedTelemetryClient, mockedHttpCallback);
    List<Event> mockedList = mock(List.class);
    theMapboxTelemetry.onFullQueue(mockedList);
    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback), anyBoolean());
  }

  @Test
  public void checksOnFullQueueSendEventsNotCalledWhenEmptyTelemetryClient() throws Exception {
    Context mockedContext = obtainNetworkConnectedMockedContext();
    String emptyValidAccessToken = "";
    String emptyUserAgent = "";
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, emptyValidAccessToken,
      emptyUserAgent, mockedTelemetryClient, mockedHttpCallback);
    List<Event> mockedList = mock(List.class);
    theMapboxTelemetry.onFullQueue(mockedList);
    verify(mockedTelemetryClient, never()).sendEvents(eq(mockedList), eq(mockedHttpCallback), anyBoolean());
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
  public void checksAppUserTurnstileNotQueued() throws Exception {
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetry();
    Event whitelistedEvent = new AppUserTurnstile("anySdkIdentifier", "anySdkVersion",
      false);
    theMapboxTelemetry.enable();
    theMapboxTelemetry.push(whitelistedEvent);
    assertTrue(theMapboxTelemetry.isQueueEmpty());
  }

  @Test
  public void checksAttachmentEventNotQueued() throws Exception {
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetry();
    Event whitelistedEvent = new Attachment();
    theMapboxTelemetry.enable();
    theMapboxTelemetry.push(whitelistedEvent);
    assertTrue(theMapboxTelemetry.isQueueEmpty());
  }

  @Test
  public void checkAddTelemetryListener() throws Exception {
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetry();
    TelemetryListener telemetryListener = mock(TelemetryListener.class);
    assertTrue(theMapboxTelemetry.addTelemetryListener(telemetryListener));
  }

  @Test
  public void checkRemoveTelemetryListener() throws Exception {
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetry();
    TelemetryListener telemetryListener = mock(TelemetryListener.class);
    theMapboxTelemetry.addTelemetryListener(telemetryListener);
    assertTrue(theMapboxTelemetry.removeTelemetryListener(telemetryListener));
  }

  @Test
  public void checkAddAttachmentListener() throws Exception {
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetry();
    AttachmentListener attachmentListener = mock(AttachmentListener.class);
    assertTrue(theMapboxTelemetry.addAttachmentListener(attachmentListener));
  }

  @Test
  public void checkRemoveAttachmentListener() throws Exception {
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetry();
    AttachmentListener attachmentListener = mock(AttachmentListener.class);
    theMapboxTelemetry.addAttachmentListener(attachmentListener);
    assertTrue(theMapboxTelemetry.removeAttachmentListener(attachmentListener));
  }

  @Test
  public void checkFlushIsCalled() {
    ExecutorService mockedExecutor = mock(ExecutorService.class);
    setupDirectExecutor(mockedExecutor);
    MapboxTelemetry mapboxTelemetry = obtainMapboxTelemetryWith(mockedExecutor);
    Event mockedEvent = mock(Event.class);
    when(mockedEvent.obtainType()).thenReturn(Event.Type.LOCATION);
    mapboxTelemetry.push(mockedEvent);
    mapboxTelemetry.disable();
    // Expect to flush and disable location
    verify(mockedExecutor, times(2)).execute(any(Runnable.class));
  }

  @Test
  public void checksSetBaseUrlWithValidHostAndWithConnection() throws Exception {
    Context mockedContext = obtainNetworkConnectedMockedContext();
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedTelemetryClient,
            mockedHttpCallback);
    assertTrue(theMapboxTelemetry.setBaseUrl(DEFAULT_STAGING_EVENTS_HOST));
    verify(mockedTelemetryClient, times(1)).setBaseUrl(eq(DEFAULT_STAGING_EVENTS_HOST));
  }

  @Test
  public void checksSetBaseUrlWithValidHostAndWithoutConnection() throws Exception {
    Context mockedContext = obtainNetworkNotConnectedMockedContext();
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedContext, mockedTelemetryClient,
            mockedHttpCallback);
    assertFalse(theMapboxTelemetry.setBaseUrl(DEFAULT_STAGING_EVENTS_HOST));
    verify(mockedTelemetryClient, never()).setBaseUrl(eq(DEFAULT_STAGING_EVENTS_HOST));
  }

  @Test
  public void checksSetBaseUrlWithNullHost() throws Exception {
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedTelemetryClient);
    assertFalse(theMapboxTelemetry.setBaseUrl(null));
    verify(mockedTelemetryClient, never()).setBaseUrl(any(String.class));
  }

  @Test
  public void checksSetBaseUrlWithEmptyHost() throws Exception {
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedTelemetryClient);
    assertFalse(theMapboxTelemetry.setBaseUrl(""));
    verify(mockedTelemetryClient, never()).setBaseUrl(any(String.class));
  }

  @Test
  public void checksSetBaseUrlWithInvalidHostOne() throws Exception {
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedTelemetryClient);
    assertFalse(theMapboxTelemetry.setBaseUrl("h@st.com"));
    verify(mockedTelemetryClient, never()).setBaseUrl(any(String.class));
  }

  @Test
  public void checksSetBaseUrlWithInvalidHostTwo() throws Exception {
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedTelemetryClient);
    assertFalse(theMapboxTelemetry.setBaseUrl("new host.com"));
    verify(mockedTelemetryClient, never()).setBaseUrl(any(String.class));
  }

  @Test
  public void checksSetBaseUrlWithInvalidHostThree() throws Exception {
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedTelemetryClient);
    assertFalse(theMapboxTelemetry.setBaseUrl("host..com"));
    verify(mockedTelemetryClient, never()).setBaseUrl(any(String.class));
  }

  @Test
  public void checksSetBaseUrlWithInvalidHostFour() throws Exception {
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedTelemetryClient);
    assertFalse(theMapboxTelemetry.setBaseUrl("host.c"));
    verify(mockedTelemetryClient, never()).setBaseUrl(any(String.class));
  }

  @Test
  public void checksSetBaseUrlWithInvalidHostFive() throws Exception {
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    MapboxTelemetry theMapboxTelemetry = obtainMapboxTelemetryWith(mockedTelemetryClient);
    assertFalse(theMapboxTelemetry.setBaseUrl("host.com."));
    verify(mockedTelemetryClient, never()).setBaseUrl(any(String.class));
  }

  private MapboxTelemetry obtainMapboxTelemetry() {
    ExecutorService mockedExecutor = mock(ExecutorService.class);
    setupDirectExecutor(mockedExecutor);
    return obtainMapboxTelemetryWith(mockedExecutor);
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(ExecutorService mockedExecutor) {
    MapboxTelemetry.applicationContext = obtainNetworkConnectedMockedContext();
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue eventsQueue = new EventsQueue(new ConcurrentQueue<Event>(),
      mock(FullQueueCallback.class), mock(ExecutorService.class));
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    TelemetryClient telemetryClient = mock(TelemetryClient.class);
    Callback httpCallback = mock(Callback.class);
    Clock mockedClock = mock(Clock.class);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    return new MapboxTelemetry(MapboxTelemetry.applicationContext,
      aValidAccessToken, aValidUserAgent, eventsQueue, telemetryClient, httpCallback, mockedSchedulerFlusher,
      mockedClock, telemetryEnabler, mockedExecutor);
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(TelemetryClient telemetryClient) throws IOException {
    Context mockedContext = obtainBlacklistContext();
    File mockedFile = mock(File.class);
    when(mockedContext.getFilesDir()).thenReturn(mockedFile);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    return new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, telemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      telemetryEnabler, mock(ExecutorService.class));
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, TelemetryClient telemetryClient,
                                                    Callback httpCallback) {
    MapboxTelemetry.applicationContext = context;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    ExecutorService mockedExecutor = mock(ExecutorService.class);
    setupDirectExecutor(mockedExecutor);
    return new MapboxTelemetry(context, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, telemetryClient, httpCallback, mockedSchedulerFlusher, mockedClock,
      telemetryEnabler, mockedExecutor);
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, EventsQueue eventsQueue,
                                                    TelemetryEnabler.State state) {
    MapboxTelemetry.applicationContext = context;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    telemetryEnabler.updatePreferences(state);
    ExecutorService mockedExecutor = mock(ExecutorService.class);
    setupDirectExecutor(mockedExecutor);
    return new MapboxTelemetry(context, aValidAccessToken, aValidUserAgent,
      eventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      telemetryEnabler, mockedExecutor);
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, EventsQueue eventsQueue) {
    MapboxTelemetry.applicationContext = context;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    MapboxTelemetry mapboxTelemetry = new MapboxTelemetry(context, aValidAccessToken, aValidUserAgent,
      eventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      telemetryEnabler, mock(ExecutorService.class));
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
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    return new MapboxTelemetry(context, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, schedulerFlusher, mockedClock,
      telemetryEnabler, mock(ExecutorService.class));
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, String accessToken, String userAgent) {
    MapboxTelemetry.applicationContext = context;
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    return new MapboxTelemetry(context, accessToken, userAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      telemetryEnabler, mock(ExecutorService.class));
  }

  private MapboxTelemetry obtainMapboxTelemetryWith(Context context, String accessToken, String userAgent,
                                                    TelemetryClient telemetryClient, Callback httpCallback) {
    MapboxTelemetry.applicationContext = context;
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    return new MapboxTelemetry(context, accessToken, userAgent,
      mockedEventsQueue, telemetryClient, httpCallback, mockedSchedulerFlusher, mockedClock,
      telemetryEnabler, mock(ExecutorService.class));
  }

  private static Context obtainNetworkConnectedMockedContext() {
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

  private Context obtainBlacklistContext() throws IOException {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    SharedPreferences mockedSharedPreferences = mock(SharedPreferences.class);
    when(mockedContext.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE))
      .thenReturn(mockedSharedPreferences);
    when(mockedSharedPreferences.getLong("mapboxConfigSyncTimestamp",0))
      .thenReturn(Long.valueOf(0));
    return mockedContext;
  }

  private void setupDirectExecutor(ExecutorService executor) {
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        ((Runnable) invocation.getArguments()[0]).run();
        return null;
      }
    }).when(executor).execute(any(Runnable.class));
  }
}