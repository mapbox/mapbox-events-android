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
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    Event.Type load = Event.Type.MAP_LOAD;
    Event aLoadEvent = obtainMapEvent(load);
    List<Event> theLoadEvent = obtainEvents(aLoadEvent);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(theLoadEvent, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), theLoadEvent.get(0));
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingMapClickEvent() throws Exception {
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    Event.Type click = Event.Type.MAP_CLICK;
    Event aClickEvent = obtainMapEvent(click);
    List<Event> theClickEvent = obtainEvents(aClickEvent);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(theClickEvent, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), theClickEvent.get(0));
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingMapDragendEvent() throws Exception {
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    Event.Type dragend = Event.Type.MAP_DRAGEND;
    Event aDragendEvent = obtainMapEvent(dragend);
    List<Event> theDragendEvent = obtainEvents(aDragendEvent);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(theDragendEvent, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), theDragendEvent.get(0));
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingMultipleEvents() throws Exception {
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    Event.Type load = Event.Type.MAP_LOAD;
    Event loadEvent = obtainMapEvent(load);
    Event.Type click = Event.Type.MAP_CLICK;
    Event clickEvent = obtainMapEvent(click);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();
    List<Event> events = obtainEvents(loadEvent, clickEvent);

    telemetryClient.sendEvents(events, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), events.get(0), events.get(1));
    assertRequestBodyEquals(expectedRequestBody);
  }

  private Event obtainLoadEvent() {
    Context mockedContext = obtainMockedContext();
    WindowManager mockedWindowManager = mock(WindowManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockedWindowManager);
    MapEventFactory mapEventFactory = new MapEventFactory(mockedContext);
    Event loadEvent = mapEventFactory.createMapLoadEvent(Event.Type.MAP_LOAD);
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
    Event clickEvent = mapEventFactory.createMapGestureEvent(Event.Type.MAP_CLICK, mapState);
    return clickEvent;
  }

  private Event obtainDragendEvent() {
    Context mockedContext = obtainMockedContext();
    MapEventFactory mapEventFactory = new MapEventFactory(mockedContext);
    MapState mapState = obtainDefaultMapState();
    Event dragendEvent = mapEventFactory.createMapGestureEvent(Event.Type.MAP_DRAGEND, mapState);
    return dragendEvent;
  }

  private Event obtainMapEvent(Event.Type type) {
    return OBTAIN_MAP_EVENT.get(type).obtain();
  }

  interface ObtainMapEvent {
    Event obtain();
  }
}
