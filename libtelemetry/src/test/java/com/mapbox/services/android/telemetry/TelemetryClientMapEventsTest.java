package com.mapbox.services.android.telemetry;


import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TelemetryClientMapEventsTest extends MockWebServerTest {

  private final Map<Event.Type, ObtainMapEvent> OBTAIN_MAP_EVENT = new HashMap<Event.Type, ObtainMapEvent>() {
    {
      put(Event.Type.MAP_LOAD, new ObtainMapEvent() {
        @Override
        public Event obtain() {
          return obtainLoadEvent();
        }
      });
      put(Event.Type.MAP_CLICK, new ObtainMapEvent() {
        @Override
        public Event obtain() {
          return obtainClickEvent();
        }
      });
      put(Event.Type.MAP_DRAGEND, new ObtainMapEvent() {
        @Override
        public Event obtain() {
          return obtainDragendEvent();
        }
      });
    }
  };

  @Test
  public void sendsTheCorrectBodyPostingMapLoadEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type load = Event.Type.MAP_LOAD;
    Event loadEvent = obtainMapEvent(load);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(loadEvent, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), loadEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingMapClickEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type click = Event.Type.MAP_CLICK;
    Event clickEvent = obtainMapEvent(click);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(clickEvent, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), clickEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingMapDragendEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type dragend = Event.Type.MAP_DRAGEND;
    Event dragendEvent = obtainMapEvent(dragend);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(dragendEvent, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), dragendEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingMultipleEvents() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type load = Event.Type.MAP_LOAD;
    Event loadEvent = obtainMapEvent(load);
    Event.Type click = Event.Type.MAP_CLICK;
    Event clickEvent = obtainMapEvent(click);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();
    List<Event> events = addEvents(loadEvent, clickEvent);

    telemetryClient.sendEvents(events, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), loadEvent, clickEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  private Event obtainLoadEvent() {
    Context mockedContext = obtainMockedContext();
    WindowManager mockedWindowManager = mock(WindowManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockedWindowManager);
    MapEventFactory mapEventFactory = new MapEventFactory(mockedContext);
    MapState mapState = obtainDefaultMapState();
    Event loadEvent = mapEventFactory.createMapEvent(Event.Type.MAP_LOAD, mapState);
    return loadEvent;
  }

  private Context obtainMockedContext() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    TelephonyManager mockedTelephonyManager = mock(TelephonyManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockedTelephonyManager);
    return mockedContext;
  }

  private MapState obtainDefaultMapState() {
    float aLatitude = 40.416775f;
    float aLongitude = -3.703790f;
    float aZoom = 1.5f;
    return new MapState(aLatitude, aLongitude, aZoom);
  }

  private Event obtainClickEvent() {
    Context mockedContext = obtainMockedContext();
    MapEventFactory mapEventFactory = new MapEventFactory(mockedContext);
    MapState mapState = obtainDefaultMapState();
    Event clickEvent = mapEventFactory.createMapEvent(Event.Type.MAP_CLICK, mapState);
    return clickEvent;
  }

  private Event obtainDragendEvent() {
    Context mockedContext = obtainMockedContext();
    MapEventFactory mapEventFactory = new MapEventFactory(mockedContext);
    MapState mapState = obtainDefaultMapState();
    Event dragendEvent = mapEventFactory.createMapEvent(Event.Type.MAP_DRAGEND, mapState);
    return dragendEvent;
  }

  private Event obtainMapEvent(Event.Type type) {
    return OBTAIN_MAP_EVENT.get(type).obtain();
  }

  interface ObtainMapEvent {
    Event obtain();
  }
}
