package com.mapbox.android.telemetry;

import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class SerializerTest {

  @Before
  public void setupMapboxTelemetry() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    AudioManager mockedAudioManager = mock(AudioManager.class, RETURNS_DEEP_STUBS);
    NavigationUtils.audioManager = mockedAudioManager;
    TelephonyManager mockedTelephonyManager = mock(TelephonyManager.class, RETURNS_DEEP_STUBS);
    TelemetryUtils.telephonyManager = mockedTelephonyManager;
  }

  @Test
  public void checkArriveSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22, 180, "sdkIdentifier", "sdkVersion",
      3, "sessionID", 10.5, 15.67, "geometry", "profile", false, "AndroidLocationEngine", 50);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));
    NavigationState navigationState = new NavigationState(metadata);

    NavigationArriveEvent navigationArriveEvent = new NavigationArriveEvent(navigationState);
    GsonBuilder gsonBuilder = new GsonBuilder();

    JsonSerializer<NavigationArriveEvent> serializer = new ArriveEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationArriveEvent.class, serializer);

    Gson customGson = gsonBuilder.create();
    String payload = customGson.toJson(navigationArriveEvent);

    String expectedJson = "{\"event\":\"navigation.arrive\",\"absoluteDistanceToDestination\":50,\"startTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,"
      + "\"durationRemaining\":180,\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdentifier\",\"sdkVersion\":\"sdkVersion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":false,"
      + "\"audioType\":\"unknown\",\"locationEngine\":\"AndroidLocationEngine\",\"volumeLevel\":0,"
      + "\"screenBrightness\":0,\"batteryPluggedIn\":false,\"batteryLevel\":0,\"connectivity\":\"Unknown\"}";

    assertEquals(expectedJson, payload);
  }

  @Test
  public void checkDepartSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22, 180, "sdkIdentifier", "sdkVersion",
      3, "sessionID", 10.5, 15.67, "geometry", "profile", false, "AndroidLocationEngine", 50);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));
    NavigationState navigationState = new NavigationState(metadata);

    NavigationDepartEvent navigationDepartEvent = new NavigationDepartEvent(navigationState);

    GsonBuilder gsonBuilder = new GsonBuilder();

    JsonSerializer<NavigationDepartEvent> serializer = new DepartEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationDepartEvent.class, serializer);

    Gson customGson = gsonBuilder.create();
    String payload = customGson.toJson(navigationDepartEvent);

    String expectedJson = "{\"absoluteDistanceToDestination\":50,\"startTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,"
      + "\"durationRemaining\":180,\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdentifier\",\"sdkVersion\":\"sdkVersion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":false,"
      + "\"audioType\":\"unknown\",\"locationEngine\":\"AndroidLocationEngine\",\"volumeLevel\":0,"
      + "\"screenBrightness\":0,\"batteryPluggedIn\":false,\"batteryLevel\":0,\"connectivity\":\"Unknown\","
      + "\"event\":\"navigation.depart\"}";

    assertEquals(expectedJson, payload);
  }


  @Test
  public void checkCancelSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22,
      180, "sdkIdentifier", "sdkVersion", 3, "sessionID", 10.5,
      15.67, "geometry", "profile", false,
      "AndroidLocationEngine", 50);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));

    NavigationCancelData navigationCancelData =
      new NavigationCancelData(TelemetryUtils.generateCreateDateFormatted(testDate));
    navigationCancelData.setComment("Test");
    navigationCancelData.setRating(75);

    NavigationState navigationState = new NavigationState(metadata);
    navigationState.setNavigationCancelData(navigationCancelData);

    NavigationCancelEvent navigationCancelEvent = new NavigationCancelEvent(navigationState);
    GsonBuilder gsonBuilder = new GsonBuilder();

    JsonSerializer<NavigationCancelEvent> serializer = new CancelEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationCancelEvent.class, serializer);

    Gson customGson = gsonBuilder.create();
    String payload = customGson.toJson(navigationCancelEvent);

    String expectedJson = "{\"event\":\"navigation.cancel\",\"arrivalTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"rating\":75,\"comment\":\"Test\","
      + "\"absoluteDistanceToDestination\":50,\"startTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,"
      + "\"durationRemaining\":180,\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdentifier\",\"sdkVersion\":\"sdkVersion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":false,"
      + "\"audioType\":\"unknown\",\"locationEngine\":\"AndroidLocationEngine\",\"volumeLevel\":0,"
      + "\"screenBrightness\":0,\"batteryPluggedIn\":false,\"batteryLevel\":0,\"connectivity\":\"Unknown\"}";

    assertEquals(expectedJson, payload);
  }

  @Test
  public void checkFeedbackSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22, 180, "sdkIdentifier", "sdkVersion",
      3, "sessionID", 10.5, 15.67, "geometry", "profile", false, "AndroidLocationEngine", 50);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));
    FeedbackEventData navigationFeedbackData = new FeedbackEventData("userId", "general",
      "unknown", "audio");
    FeedbackData feedbackData = new FeedbackData();
    Location[] locationsBefore = new Location[1];
    locationsBefore[0] = mock(Location.class);
    Location[] locationsAfter = new Location[1];
    locationsAfter[0] = mock(Location.class);
    NavigationLocationData navigationLocationData = new NavigationLocationData(locationsBefore, locationsAfter);

    NavigationState navigationState = new NavigationState(metadata);
    navigationState.setNavigationLocationData(navigationLocationData);
    navigationState.setFeedbackEventData(navigationFeedbackData);
    navigationState.setFeedbackData(feedbackData);

    NavigationFeedbackEvent navigationFeedbackEvent = new NavigationFeedbackEvent(navigationState);
    GsonBuilder gsonBuilder = new GsonBuilder();

    JsonSerializer<NavigationFeedbackEvent> serializer = new FeedbackEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationFeedbackEvent.class, serializer);

    Gson customGson = gsonBuilder.create();
    String payload = customGson.toJson(navigationFeedbackEvent);

    String expectedJson = "{\"event\":\"navigation.feedback\",\"absoluteDistanceToDestination\":50,"
      + "\"startTimestamp\":\"" + TelemetryUtils.generateCreateDateFormatted(testDate)
      + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,\"durationRemaining\":180,"
      + "\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdentifier\",\"sdkVersion\":\"sdkVersion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":false,"
      + "\"audioType\":\"unknown\",\"locationEngine\":\"AndroidLocationEngine\",\"volumeLevel\":0,"
      + "\"screenBrightness\":0,\"batteryPluggedIn\":false,\"batteryLevel\":0,\"connectivity\":\"Unknown\","
      + "\"userId\":\"userId\",\"feedbackType\":\"general\",\"source\":\"unknown\",\"audio\":\"audio\","
      + "\"locationsBefore\":[{}],\"locationsAfter\":[{}],\"feedbackId\":\"" + feedbackData.getFeedbackId() + "\"}";

    assertEquals(expectedJson, payload);
  }

  @Test
  public void checkRerouteSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22,
      180, "sdkIdent", "sdkversion", 3, "sessionID", 10.5,
      15.67, "geometry", "profile", true,
      "MockLocationEngine", 1300);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));

    NavigationNewData navigationNewData = new NavigationNewData(100, 750,
      "mewGeometry");
    NavigationRerouteData navigationRerouteData = new NavigationRerouteData(navigationNewData, 12000);
    FeedbackData feedbackData = new FeedbackData();
    feedbackData.setScreenshot("screenshot");

    NavigationStepMetadata navigationStepMetadata = new NavigationStepMetadata();
    navigationStepMetadata.setUpcomingInstruction("upcomingInstruction");
    navigationStepMetadata.setUpcomingType("upcomingType");
    navigationStepMetadata.setUpcomingModifier("upcomingModifier");
    navigationStepMetadata.setUpcomingName("upcomingName");
    navigationStepMetadata.setPreviousInstruction("previousInstruction");
    navigationStepMetadata.setPreviousType("previousType");
    navigationStepMetadata.setPreviousModifier("previousModifier");
    navigationStepMetadata.setPreviousName("previousName");
    navigationStepMetadata.setDistance(100);
    navigationStepMetadata.setDuration(1200);
    navigationStepMetadata.setDistanceRemaining(250);
    navigationStepMetadata.setDurationRemaining(2200);

    Location[] locationsBefore = new Location[1];
    Location[] locationsAfter = new Location[1];
    NavigationLocationData navigationLocationData = new NavigationLocationData(locationsBefore, locationsAfter);

    NavigationState navigationState = new NavigationState(metadata);
    navigationState.setNavigationLocationData(navigationLocationData);
    navigationState.setNavigationRerouteData(navigationRerouteData);
    navigationState.setFeedbackData(feedbackData);
    navigationState.setNavigationStepMetadata(navigationStepMetadata);

    NavigationRerouteEvent navigationRerouteEvent = new NavigationRerouteEvent(navigationState);
    GsonBuilder gsonBuilder = new GsonBuilder();

    JsonSerializer<NavigationRerouteEvent> serializer = new RerouteEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationRerouteEvent.class, serializer);

    Gson customGson = gsonBuilder.create();
    String payload = customGson.toJson(navigationRerouteEvent);

    String expectedJson = "{\"event\":\"navigation.reroute\",\"absoluteDistanceToDestination\":1300,"
      + "\"startTimestamp\":\"" + TelemetryUtils.generateCreateDateFormatted(testDate)
      + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,"
      + "\"durationRemaining\":180,\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdent\",\"sdkVersion\":\"sdkversion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":true,"
      + "\"audioType\":\"unknown\",\"locationEngine\":\"MockLocationEngine\",\"volumeLevel\":0,\"screenBrightness\":0,"
      + "\"batteryPluggedIn\":false,\"batteryLevel\":0,\"connectivity\":\"Unknown\",\"navigationNewData\":"
      + "{\"newDistanceRemaining\":100,\"newDurationRemaining\":750,\"newGeometry\":\"mewGeometry\"},"
      + "\"secondsSinceLastReroute\":12000,\"locationsBefore\":[null],\"locationsAfter\":[null],"
      + "\"feedbackId\":\"" + feedbackData.getFeedbackId() + "\",\"screenshot\":\"screenshot\",\"step\":"
      + "{\"upcomingInstruction\":\"upcomingInstruction\",\"upcomingType\":\"upcomingType\","
      + "\"upcomingModifier\":\"upcomingModifier\",\"upcomingName\":\"upcomingName\","
      + "\"previousInstruction\":\"previousInstruction\",\"previousType\":\"previousType\","
      + "\"previousModifier\":\"previousModifier\",\"previousName\":\"previousName\",\"distance\":100,"
      + "\"duration\":1200,\"distanceRemaining\":250,\"durationRemaining\":2200}}";

    assertEquals(expectedJson, payload);
  }

  @Test
  public void checkFasterRouteSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22,
      180, "sdkIdent", "sdkversion", 3, "sessionID", 10.5,
      15.67, "geometry", "profile", true,
      "MockLocationEngine", 1300);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));
    NavigationNewData navigationNewData = new NavigationNewData(100, 750,
      "mewGeometry");
    NavigationRerouteData navigationRerouteData = new NavigationRerouteData(navigationNewData, 12000);

    NavigationStepMetadata navigationStepMetadata = new NavigationStepMetadata();
    navigationStepMetadata.setUpcomingInstruction("upcomingInstruction");
    navigationStepMetadata.setUpcomingType("upcomingType");
    navigationStepMetadata.setUpcomingModifier("upcomingModifier");
    navigationStepMetadata.setUpcomingName("upcomingName");
    navigationStepMetadata.setPreviousInstruction("previousInstruction");
    navigationStepMetadata.setPreviousType("previousType");
    navigationStepMetadata.setPreviousModifier("previousModifier");
    navigationStepMetadata.setPreviousName("previousName");
    navigationStepMetadata.setDistance(100);
    navigationStepMetadata.setDuration(1200);
    navigationStepMetadata.setDistanceRemaining(250);
    navigationStepMetadata.setDurationRemaining(2200);

    NavigationState navigationState = new NavigationState(metadata);
    navigationState.setNavigationRerouteData(navigationRerouteData);
    navigationState.setNavigationStepMetadata(navigationStepMetadata);

    NavigationFasterRouteEvent navigationFasterRouteEvent = new NavigationFasterRouteEvent(navigationState);
    GsonBuilder gsonBuilder = new GsonBuilder();

    JsonSerializer<NavigationFasterRouteEvent> serializer = new FasterRouteEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationFasterRouteEvent.class, serializer);

    Gson customGson = gsonBuilder.create();
    String payload = customGson.toJson(navigationFasterRouteEvent);

    String expectedJson = "{\"event\":\"navigation.fasterRoute\",\"absoluteDistanceToDestination\":1300,"
      + "\"startTimestamp\":\"" + TelemetryUtils.generateCreateDateFormatted(testDate)
      + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,"
      + "\"durationRemaining\":180,\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdent\",\"sdkVersion\":\"sdkversion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":true,"
      + "\"audioType\":\"unknown\",\"locationEngine\":\"MockLocationEngine\",\"volumeLevel\":0,\"screenBrightness\":0,"
      + "\"batteryPluggedIn\":false,\"batteryLevel\":0,\"connectivity\":\"Unknown\",\"newDistanceRemaining\":100,"
      + "\"newDurationRemaining\":750,\"newGeometry\":\"mewGeometry\",\"step\":"
      + "{\"upcomingInstruction\":\"upcomingInstruction\",\"upcomingType\":\"upcomingType\","
      + "\"upcomingModifier\":\"upcomingModifier\",\"upcomingName\":\"upcomingName\",\"previousInstruction\":"
      + "\"previousInstruction\",\"previousType\":\"previousType\",\"previousModifier\":\"previousModifier\","
      + "\"previousName\":\"previousName\",\"distance\":100,\"duration\":1200,\"distanceRemaining\":250,"
      + "\"durationRemaining\":2200}}";

    assertEquals(expectedJson, payload);
  }
}