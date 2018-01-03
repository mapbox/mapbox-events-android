package com.mapbox.services.android.telemetry;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapEventFactoryTest {

  @Test
  public void checksMapLoadEvent() throws Exception {
    Context mockedContext = obtainMockedContext();
    MapEventFactory aMapEventFactory = new MapEventFactory(mockedContext);

    Event mapLoadEvent = aMapEventFactory.createMapLoadEvent(Event.Type.MAP_LOAD);

    assertTrue(mapLoadEvent instanceof MapLoadEvent);
  }

  @Test
  public void checksLoadType() throws Exception {
    Context mockedContext = obtainMockedContext();
    MapEventFactory aMapEventFactory = new MapEventFactory(mockedContext);

    Event mapLoadEvent = aMapEventFactory.createMapLoadEvent(Event.Type.MAP_LOAD);

    assertEquals(Event.Type.MAP_LOAD, mapLoadEvent.obtainType());
  }

  @Test
  public void checksMapClickEvent() throws Exception {
    Context mockedContext = obtainMockedContext();
    MapEventFactory aMapEventFactory = new MapEventFactory(mockedContext);
    MapState mockedMapState = mock(MapState.class);

    Event mapClickEvent = aMapEventFactory.createMapGestureEvent(Event.Type.MAP_CLICK, mockedMapState);

    assertTrue(mapClickEvent instanceof MapClickEvent);
  }

  @Test
  public void checksClickType() throws Exception {
    Context mockedContext = obtainMockedContext();
    MapEventFactory aMapEventFactory = new MapEventFactory(mockedContext);
    MapState mockedMapState = mock(MapState.class);

    Event mapClickEvent = aMapEventFactory.createMapGestureEvent(Event.Type.MAP_CLICK, mockedMapState);

    assertEquals(Event.Type.MAP_CLICK, mapClickEvent.obtainType());
  }

  @Test
  public void checksMapDragendEvent() throws Exception {
    Context mockedContext = obtainMockedContext();
    MapEventFactory aMapEventFactory = new MapEventFactory(mockedContext);
    MapState mockedMapState = mock(MapState.class);

    Event mapDragendEvent = aMapEventFactory.createMapGestureEvent(Event.Type.MAP_DRAGEND, mockedMapState);

    assertTrue(mapDragendEvent instanceof MapDragendEvent);
  }

  @Test
  public void checksDragendType() throws Exception {
    Context mockedContext = obtainMockedContext();
    MapEventFactory aMapEventFactory = new MapEventFactory(mockedContext);
    MapState mockedMapState = mock(MapState.class);

    Event mapDragendEvent = aMapEventFactory.createMapGestureEvent(Event.Type.MAP_DRAGEND, mockedMapState);

    assertEquals(Event.Type.MAP_DRAGEND, mapDragendEvent.obtainType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksLoadInvalidType() throws Exception {
    Context mockedContext = obtainMockedContext();
    MapEventFactory aMapEventFactory = new MapEventFactory(mockedContext);
    Event.Type notALoadMapType = Event.Type.MAP_CLICK;

    aMapEventFactory.createMapLoadEvent(notALoadMapType);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksGestureInvalidType() throws Exception {
    Context mockedContext = obtainMockedContext();
    MapEventFactory aMapEventFactory = new MapEventFactory(mockedContext);
    Event.Type notAGestureMapType = Event.Type.MAP_LOAD;
    MapState mockedMapState = mock(MapState.class);

    aMapEventFactory.createMapGestureEvent(notAGestureMapType, mockedMapState);
  }

  @Test
  public void checksValidMapState() throws Exception {
    Context mockedContext = obtainMockedContext();
    MapEventFactory aMapEventFactory = new MapEventFactory(mockedContext);
    Event.Type aDragendMapEventType = Event.Type.MAP_DRAGEND;
    MapState aValidMapState = obtainAValidMapState();

    Event aDragendMapEvent = aMapEventFactory.createMapGestureEvent(aDragendMapEventType, aValidMapState);

    assertTrue(aDragendMapEvent instanceof MapDragendEvent);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksInvalidMapState() throws Exception {
    Context mockedContext = obtainMockedContext();
    MapEventFactory aMapEventFactory = new MapEventFactory(mockedContext);
    Event.Type aDragendMapEventType = Event.Type.MAP_DRAGEND;
    MapState nullMapState = null;

    aMapEventFactory.createMapGestureEvent(aDragendMapEventType, nullMapState);
  }

  private Context obtainMockedContext() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    TelephonyManager mockedTelephonyManager = mock(TelephonyManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockedTelephonyManager);
    WindowManager mockedWindowManager = mock(WindowManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockedWindowManager);
    return mockedContext;
  }

  private MapState obtainAValidMapState() {
    float aLatitude = 40.416775f;
    float aLongitude = -3.703790f;
    float aZoom = 1.5f;
    return new MapState(aLatitude, aLongitude, aZoom);
  }
}