package com.mapbox.android.telemetry;

import android.app.AlarmManager;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import org.junit.Test;

import okhttp3.Callback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapEventFactoryTest {

  @Test(expected = IllegalStateException.class)
  public void checksMapboxTelemetryNotInitialized() throws Exception {
    MapboxTelemetry.applicationContext = null;

    new MapEventFactory();
  }

  @Test
  public void checksMapLoadEvent() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();

    Event mapLoadEvent = aMapEventFactory.createMapLoadEvent(Event.Type.MAP_LOAD);

    assertTrue(mapLoadEvent instanceof MapLoadEvent);
  }

  @Test
  public void checksLoadType() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();

    Event mapLoadEvent = aMapEventFactory.createMapLoadEvent(Event.Type.MAP_LOAD);

    assertEquals(Event.Type.MAP_LOAD, mapLoadEvent.obtainType());
  }

  @Test
  public void checksMapClickEvent() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();
    MapState mockedMapState = mock(MapState.class);

    Event mapClickEvent = aMapEventFactory.createMapGestureEvent(Event.Type.MAP_CLICK, mockedMapState);

    assertTrue(mapClickEvent instanceof MapClickEvent);
  }

  @Test
  public void checksClickType() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();
    MapState mockedMapState = mock(MapState.class);

    Event mapClickEvent = aMapEventFactory.createMapGestureEvent(Event.Type.MAP_CLICK, mockedMapState);

    assertEquals(Event.Type.MAP_CLICK, mapClickEvent.obtainType());
  }

  @Test
  public void checksMapDragendEvent() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();
    MapState mockedMapState = mock(MapState.class);

    Event mapDragendEvent = aMapEventFactory.createMapGestureEvent(Event.Type.MAP_DRAGEND, mockedMapState);

    assertTrue(mapDragendEvent instanceof MapDragendEvent);
  }

  @Test
  public void checksDragendType() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();
    MapState mockedMapState = mock(MapState.class);

    Event mapDragendEvent = aMapEventFactory.createMapGestureEvent(Event.Type.MAP_DRAGEND, mockedMapState);

    assertEquals(Event.Type.MAP_DRAGEND, mapDragendEvent.obtainType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksLoadInvalidType() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();
    Event.Type notALoadMapType = Event.Type.MAP_CLICK;

    aMapEventFactory.createMapLoadEvent(notALoadMapType);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksGestureInvalidType() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();
    Event.Type notAGestureMapType = Event.Type.MAP_LOAD;
    MapState mockedMapState = mock(MapState.class);

    aMapEventFactory.createMapGestureEvent(notAGestureMapType, mockedMapState);
  }

  @Test
  public void checksValidMapState() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();
    Event.Type aDragendMapEventType = Event.Type.MAP_DRAGEND;
    MapState aValidMapState = obtainAValidMapState();

    Event aDragendMapEvent = aMapEventFactory.createMapGestureEvent(aDragendMapEventType, aValidMapState);

    assertTrue(aDragendMapEvent instanceof MapDragendEvent);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksInvalidMapState() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();
    Event.Type aDragendMapEventType = Event.Type.MAP_DRAGEND;
    MapState nullMapState = null;

    aMapEventFactory.createMapGestureEvent(aDragendMapEventType, nullMapState);
  }

  @Test
  public void checksOfflineDownloadStartEvent() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();

    Event offlineEvent =
      aMapEventFactory.createOfflineDownloadStartEvent("tileregion",
              3.0, 7.0, "mapbox.mapbox-streets-v7");

    assertTrue(offlineEvent instanceof OfflineDownloadStartEvent);
  }

  @Test
  public void checksOfflineDownloadCompleteEvent() throws Exception {
    initializeMapboxTelemetry();
    MapEventFactory aMapEventFactory = new MapEventFactory();

    Event offlineEvent = aMapEventFactory.createOfflineDownloadCompleteEvent(
            "tileregion",3.0, 7.0,
      "mapbox.mapbox-streets-v7",
            5L, 1000L, "complete");

    assertTrue(offlineEvent instanceof OfflineDownloadEndEvent);
  }

  private void initializeMapboxTelemetry() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    TelephonyManager mockedTelephonyManager = mock(TelephonyManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockedTelephonyManager);
    WindowManager mockedWindowManager = mock(WindowManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockedWindowManager);
    AlarmManager mockedAlarmManager = mock(AlarmManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockedAlarmManager);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    boolean indifferentServiceBound = true;
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    TelemetryLocationEnabler telemetryLocationEnabler = new TelemetryLocationEnabler(false);
    new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      indifferentServiceBound, telemetryEnabler, telemetryLocationEnabler);
  }

  private MapState obtainAValidMapState() {
    double aLatitude = 40.416775d;
    double aLongitude = -3.703790d;
    double aZoom = 1.5d;
    return new MapState(aLatitude, aLongitude, aZoom);
  }
}