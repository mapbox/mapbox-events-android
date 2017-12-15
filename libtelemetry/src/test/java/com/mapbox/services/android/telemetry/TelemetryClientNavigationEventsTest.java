package com.mapbox.services.android.telemetry;


import android.location.Location;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;

import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;

import static org.mockito.Mockito.mock;

public class TelemetryClientNavigationEventsTest extends MockWebServerTest {

  private final Map<Event.Type, ObtainNavEvent> OBTAIN_NAVIGATION_EVENT = new HashMap<Event.Type, ObtainNavEvent>() {
    {
      put(Event.Type.NAV_ARRIVE, new ObtainNavEvent() {
        @Override
        public Event obtain() {
          return obtainArriveEvent();
        }
      });
      put(Event.Type.NAV_DEPART, new ObtainNavEvent() {
        @Override
        public Event obtain() {
          return obtainDepartEvent();
        }
      });
      put(Event.Type.NAV_CANCEL, new ObtainNavEvent() {
        @Override
        public Event obtain() {
          return obtainCancelEvent();
        }
      });
      put(Event.Type.NAV_FEEDBACK, new ObtainNavEvent() {
        @Override
        public Event obtain() {
          return obtainFeedbackEvent();
        }
      });
      put(Event.Type.NAV_REROUTE, new ObtainNavEvent() {
        @Override
        public Event obtain() {
          return obtainRerouteEvent();
        }
      });
      put(Event.Type.NAV_FASTER_ROUTE, new ObtainNavEvent() {
        @Override
        public Event obtain() {
          return obtainFasterRouteEvent();
        }
      });
    }
  };
  private final Map<Event.Type, ConfigureTypeAdapter> CONFIGURE_TYPE_ADAPTER =
    new HashMap<Event.Type, ConfigureTypeAdapter>() {
      {
        put(Event.Type.NAV_ARRIVE, new ConfigureTypeAdapter() {
          @Override
          public GsonBuilder configure(GsonBuilder gsonBuilder) {
            return configureArriveTypeAdapter(gsonBuilder);
          }
        });
        put(Event.Type.NAV_DEPART, new ConfigureTypeAdapter() {
          @Override
          public GsonBuilder configure(GsonBuilder gsonBuilder) {
            return configureDepartTypeAdapter(gsonBuilder);
          }
        });
        put(Event.Type.NAV_CANCEL, new ConfigureTypeAdapter() {
          @Override
          public GsonBuilder configure(GsonBuilder gsonBuilder) {
            return configureCancelTypeAdapter(gsonBuilder);
          }
        });
        put(Event.Type.NAV_FEEDBACK, new ConfigureTypeAdapter() {
          @Override
          public GsonBuilder configure(GsonBuilder gsonBuilder) {
            return configureFeedbackTypeAdapter(gsonBuilder);
          }
        });
        put(Event.Type.NAV_REROUTE, new ConfigureTypeAdapter() {
          @Override
          public GsonBuilder configure(GsonBuilder gsonBuilder) {
            return configureRerouteTypeAdapter(gsonBuilder);
          }
        });
        put(Event.Type.NAV_FASTER_ROUTE, new ConfigureTypeAdapter() {
          @Override
          public GsonBuilder configure(GsonBuilder gsonBuilder) {
            return configureFasterRouteTypeAdapter(gsonBuilder);
          }
        });
      }
    };

  @Test
  public void sendsTheCorrectBodyPostingAppUserTurnstileEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    boolean indifferentTelemetryEnabled = false;
    Event theAppUserTurnstile = new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(theAppUserTurnstile, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), theAppUserTurnstile);
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingNavigationArriveEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type arrive = Event.Type.NAV_ARRIVE;
    Event arriveEvent = obtainNavigationEvent(arrive);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(arriveEvent, mockedCallback);

    GsonBuilder gsonBuilder = configureTypeAdapter(arrive, new GsonBuilder());
    String expectedRequestBody = obtainExpectedRequestBody(gsonBuilder, arriveEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingNavigationDepartEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type depart = Event.Type.NAV_DEPART;
    Event departEvent = obtainNavigationEvent(depart);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(departEvent, mockedCallback);

    GsonBuilder gsonBuilder = configureTypeAdapter(depart, new GsonBuilder());
    String expectedRequestBody = obtainExpectedRequestBody(gsonBuilder, departEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingNavigationCancelEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type cancel = Event.Type.NAV_CANCEL;
    Event cancelEvent = obtainNavigationEvent(cancel);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(cancelEvent, mockedCallback);

    GsonBuilder gsonBuilder = configureTypeAdapter(cancel, new GsonBuilder());
    String expectedRequestBody = obtainExpectedRequestBody(gsonBuilder, cancelEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingNavigationFeedbackEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type feedback = Event.Type.NAV_FEEDBACK;
    Event feedbackEvent = obtainNavigationEvent(feedback);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(feedbackEvent, mockedCallback);

    GsonBuilder gsonBuilder = configureTypeAdapter(feedback, new GsonBuilder());
    String expectedRequestBody = obtainExpectedRequestBody(gsonBuilder, feedbackEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingNavigationRerouteEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type reroute = Event.Type.NAV_REROUTE;
    Event rerouteEvent = obtainNavigationEvent(reroute);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(rerouteEvent, mockedCallback);

    GsonBuilder gsonBuilder = configureTypeAdapter(reroute, new GsonBuilder());
    String expectedRequestBody = obtainExpectedRequestBody(gsonBuilder, rerouteEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingNavigationFasterRouteEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type fasterRoute = Event.Type.NAV_FASTER_ROUTE;
    Event fasterRouteEvent = obtainNavigationEvent(fasterRoute);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(fasterRouteEvent, mockedCallback);

    GsonBuilder gsonBuilder = configureTypeAdapter(fasterRoute, new GsonBuilder());
    String expectedRequestBody = obtainExpectedRequestBody(gsonBuilder, fasterRouteEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  @Test
  public void sendsTheCorrectBodyPostingMultipleEvents() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    Event.Type reroute = Event.Type.NAV_REROUTE;
    Event rerouteEvent = obtainNavigationEvent(reroute);
    Event.Type fasterRoute = Event.Type.NAV_FASTER_ROUTE;
    Event fasterRouteEvent = obtainNavigationEvent(fasterRoute);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();
    List<Event> events = addEvents(rerouteEvent, fasterRouteEvent);

    telemetryClient.sendEvents(events, mockedCallback);

    GsonBuilder gsonBuilder = configureTypeAdapter(reroute, new GsonBuilder());
    gsonBuilder = configureTypeAdapter(fasterRoute, gsonBuilder);
    String expectedRequestBody = obtainExpectedRequestBody(gsonBuilder, rerouteEvent, fasterRouteEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }

  private NavigationState obtainDefaultNavigationState(Date date) {
    NavigationMetadata metadata = new NavigationMetadata(date, 13, 22, 180, "sdkIdentifier", "sdkVersion",
      "sessionID", 10.5, 15.67, "geometry", "profile", false, "device", "LostLocationEngine", 50);
    return new NavigationState(metadata);
  }

  private Event obtainNavigationEvent(Event.Type type) {
    return OBTAIN_NAVIGATION_EVENT.get(type).obtain();
  }

  private Event obtainArriveEvent() {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    Date aDate = new Date();
    NavigationState navigationState = obtainDefaultNavigationState(aDate);
    Event arriveEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_ARRIVE, navigationState);
    return arriveEvent;
  }

  private Event obtainDepartEvent() {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    Date aDate = new Date();
    NavigationState navigationState = obtainDefaultNavigationState(aDate);
    Event departEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_DEPART, navigationState);
    return departEvent;
  }

  private Event obtainCancelEvent() {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    Date aDate = new Date();
    NavigationState navigationState = obtainDefaultNavigationState(aDate);
    NavigationCancelData navigationCancelData =
      new NavigationCancelData(TelemetryUtils.generateCreateDateFormatted(aDate));
    navigationState.setNavigationCancelData(navigationCancelData);
    Event cancelEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_CANCEL, navigationState);
    return cancelEvent;
  }

  private Event obtainFeedbackEvent() {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    Date aDate = new Date();
    NavigationState navigationState = obtainDefaultNavigationState(aDate);
    FeedbackEventData navigationFeedbackData = new FeedbackEventData("userId", "general",
      "unknown", "audio");
    FeedbackData feedbackData = obtainFeedbackData();
    NavigationVoiceData navigationVoiceData = new NavigationVoiceData("voiceInstruction",
      TelemetryUtils.generateCreateDateFormatted(aDate));
    NavigationLocationData navigationLocationData = obtainLocationData();
    navigationState.setNavigationLocationData(navigationLocationData);
    navigationState.setFeedbackEventData(navigationFeedbackData);
    navigationState.setNavigationVoiceData(navigationVoiceData);
    navigationState.setFeedbackData(feedbackData);
    Event feedbackEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_FEEDBACK, navigationState);
    return feedbackEvent;
  }

  private NavigationLocationData obtainLocationData() {
    Location[] locationsBefore = new Location[1];
    Location[] locationsAfter = new Location[1];
    return new NavigationLocationData(locationsBefore, locationsAfter);
  }

  private Event obtainRerouteEvent() {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    Date aDate = new Date();
    NavigationState navigationState = obtainDefaultNavigationState(aDate);
    NavigationNewData navigationNewData = obtainNewData();
    NavigationRerouteData navigationRerouteData = new NavigationRerouteData(navigationNewData, 12000);
    NavigationVoiceData navigationVoiceData = new NavigationVoiceData("voiceInstruction",
      TelemetryUtils.generateCreateDateFormatted(aDate));
    FeedbackData feedbackData = obtainFeedbackData();
    NavigationStepMetadata navigationStepMetadata = new NavigationStepMetadata();
    NavigationLocationData navigationLocationData = obtainLocationData();
    navigationState.setNavigationLocationData(navigationLocationData);
    navigationState.setNavigationRerouteData(navigationRerouteData);
    navigationState.setNavigationVoiceData(navigationVoiceData);
    navigationState.setFeedbackData(feedbackData);
    navigationState.setNavigationStepMetadata(navigationStepMetadata);
    Event rerouteEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_REROUTE, navigationState);
    return rerouteEvent;
  }

  private FeedbackData obtainFeedbackData() {
    return new FeedbackData("feedbackId");
  }

  private NavigationNewData obtainNewData() {
    return new NavigationNewData(100, 750, "newGeometry");
  }

  private Event obtainFasterRouteEvent() {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    Date aDate = new Date();
    NavigationState navigationState = obtainDefaultNavigationState(aDate);
    NavigationNewData navigationNewData = obtainNewData();
    NavigationRerouteData navigationRerouteData = new NavigationRerouteData(navigationNewData, 12000);
    NavigationStepMetadata navigationStepMetadata = new NavigationStepMetadata();
    navigationState.setNavigationRerouteData(navigationRerouteData);
    navigationState.setNavigationStepMetadata(navigationStepMetadata);
    Event fasterRouteEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_FASTER_ROUTE, navigationState);
    return fasterRouteEvent;
  }

  private GsonBuilder configureTypeAdapter(Event.Type type, GsonBuilder gsonBuilder) {
    return CONFIGURE_TYPE_ADAPTER.get(type).configure(gsonBuilder);
  }

  private GsonBuilder configureArriveTypeAdapter(GsonBuilder gsonBuilder) {
    JsonSerializer<NavigationArriveEvent> arriveSerializer = new ArriveEventSerializer();
    GsonBuilder arriveGsonBuilder = gsonBuilder.registerTypeAdapter(NavigationArriveEvent.class, arriveSerializer);
    return arriveGsonBuilder;
  }

  private GsonBuilder configureDepartTypeAdapter(GsonBuilder gsonBuilder) {
    JsonSerializer<NavigationDepartEvent> serializer = new DepartEventSerializer();
    GsonBuilder departGsonBuilder = gsonBuilder.registerTypeAdapter(NavigationDepartEvent.class, serializer);
    return departGsonBuilder;
  }

  private GsonBuilder configureCancelTypeAdapter(GsonBuilder gsonBuilder) {
    JsonSerializer<NavigationCancelEvent> serializer = new CancelEventSerializer();
    GsonBuilder cancelGsonBuilder = gsonBuilder.registerTypeAdapter(NavigationCancelEvent.class, serializer);
    return cancelGsonBuilder;
  }

  private GsonBuilder configureFeedbackTypeAdapter(GsonBuilder gsonBuilder) {
    JsonSerializer<NavigationFeedbackEvent> serializer = new FeedbackEventSerializer();
    GsonBuilder feedbackGsonBuilder = gsonBuilder.registerTypeAdapter(NavigationFeedbackEvent.class, serializer);
    return feedbackGsonBuilder;
  }

  private GsonBuilder configureRerouteTypeAdapter(GsonBuilder gsonBuilder) {
    JsonSerializer<NavigationRerouteEvent> serializer = new RerouteEventSerializer();
    GsonBuilder rerouteGsonBuilder = gsonBuilder.registerTypeAdapter(NavigationRerouteEvent.class, serializer);
    return rerouteGsonBuilder;
  }

  private GsonBuilder configureFasterRouteTypeAdapter(GsonBuilder gsonBuilder) {
    JsonSerializer<NavigationFasterRouteEvent> serializer = new FasterRouteEventSerializer();
    GsonBuilder fasterRouteGsonBuilder = gsonBuilder.registerTypeAdapter(NavigationFasterRouteEvent.class, serializer);
    return fasterRouteGsonBuilder;
  }

  interface ObtainNavEvent {
    Event obtain();
  }

  interface ConfigureTypeAdapter {
    GsonBuilder configure(GsonBuilder gsonBuilder);
  }
}
