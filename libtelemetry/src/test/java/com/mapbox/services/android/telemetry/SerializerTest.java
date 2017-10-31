package com.mapbox.services.android.telemetry;

import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class SerializerTest {
  @Test
  public void checkArriveSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22, 180, "sdkIdentifier", "sdkVersion",
      "sessionID", 10.5, 15.67, "geometry", "profile", false, "device", "LostLocationEngine", 50);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));
    NavigationState navigationState = new NavigationState(metadata);

    NavigationArriveEvent navigationArriveEvent = new NavigationArriveEvent(navigationState);
    GsonBuilder gsonBuilder = new GsonBuilder();

    JsonSerializer<NavigationArriveEvent> serializer = new ArriveEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationArriveEvent.class, serializer);

    Gson customGson = gsonBuilder.create();
    String payload =  customGson.toJson(navigationArriveEvent);

    String expectedJson = "{\"absoluteDistanceToDestination\":50,\"startTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,"
      + "\"durationRemaining\":180,\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdentifier\",\"sdkVersion\":\"sdkVersion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":false,"
      + "\"device\":\"device\",\"locationEngine\":\"LostLocationEngine\"}";

    assertEquals(expectedJson, payload);
  }

  @Test
  public void checkDepartSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22, 180, "sdkIdentifier", "sdkVersion",
      "sessionID", 10.5, 15.67, "geometry", "profile", false, "device", "LostLocationEngine", 50);
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
      + "\"device\":\"device\",\"locationEngine\":\"LostLocationEngine\"}";

    assertEquals(expectedJson, payload);
  }


  @Test
  public void checkCancelSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22,
      180, "sdkIdentifier", "sdkVersion", "sessionID", 10.5,
      15.67, "geometry", "profile", false, "device",
      "LostLocationEngine", 50);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));

    NavigationCancelData navigationCancelData =
      new NavigationCancelData(TelemetryUtils.generateCreateDateFormatted(testDate));
    navigationCancelData.setComment("Test");
    navigationCancelData.setRating(75);

    NavigationState navigationState = new NavigationState(metadata);
    navigationState.setNavigationCancelData(navigationCancelData);

    NavigationCancelEvent navigationCancelEvent = new NavigationCancelEvent(navigationState);
    GsonBuilder gsonBuilder = new GsonBuilder();

    JsonSerializer<NavigationCancelEvent> serializer = new CancelSerializer();
    gsonBuilder.registerTypeAdapter(NavigationCancelEvent.class, serializer);

    Gson customGson = gsonBuilder.create();
    String payload =  customGson.toJson(navigationCancelEvent);

    String expectedJson = "{\"arrivalTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"rating\":75,\"comment\":\"Test\","
      + "\"absoluteDistanceToDestination\":50,\"startTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,"
      + "\"durationRemaining\":180,\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdentifier\",\"sdkVersion\":\"sdkVersion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":false,"
      + "\"device\":\"device\",\"locationEngine\":\"LostLocationEngine\"}";

    assertEquals(expectedJson, payload);
  }

  @Test
  public void checkFeedbackSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22, 180, "sdkIdentifier", "sdkVersion",
      "sessionID", 10.5, 15.67, "geometry", "profile", false, "device", "LostLocationEngine", 50);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));
    FeedbackEventData navigationFeedbackData = new FeedbackEventData("userId", "general",
      "unknown", "audio");
    FeedbackData feedbackData = new FeedbackData("feedbackId");
    NavigationVoiceData navigationVoiceData = new NavigationVoiceData("voiceInstruction",
      TelemetryUtils.generateCreateDateFormatted(testDate));
    Location[] locationsBefore = new Location[1];
    locationsBefore[0] = mock(Location.class);
    Location[] locationsAfter = new Location[1];
    locationsAfter[0] = mock(Location.class);
    NavigationLocationData navigationLocationData = new NavigationLocationData(locationsBefore, locationsAfter);

    NavigationState navigationState = new NavigationState(metadata);
    navigationState.setNavigationLocationData(navigationLocationData);
    navigationState.setFeedbackEventData(navigationFeedbackData);
    navigationState.setNavigationVoiceData(navigationVoiceData);
    navigationState.setFeedbackData(feedbackData);

    NavigationFeedbackEvent navigationFeedbackEvent = new NavigationFeedbackEvent(navigationState);
    GsonBuilder gsonBuilder = new GsonBuilder();

    JsonSerializer<NavigationFeedbackEvent> serializer = new FeedbackEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationFeedbackEvent.class, serializer);

    Gson customGson = gsonBuilder.create();
    String payload = customGson.toJson(navigationFeedbackEvent);

    String expectedJson = "{\"absoluteDistanceToDestination\":50,\"startTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,"
      + "\"durationRemaining\":180,\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdentifier\",\"sdkVersion\":\"sdkVersion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":false,"
      + "\"device\":\"device\",\"locationEngine\":\"LostLocationEngine\",\"userId\":\"userId\","
      + "\"feedbackType\":\"general\",\"source\":\"unknown\",\"audio\":\"audio\",\"locationsBefore\":[{}],"
      + "\"locationsAfter\":[{}],\"feedbackId\":\"feedbackId\"}";

    assertEquals(expectedJson, payload);
  }

  @Test
  public void checkRerouteSerializing() throws Exception {
    Date testDate = new Date();
    NavigationMetadata metadata = new NavigationMetadata(testDate, 13, 22,
      180, "sdkIdent","sdkversion", "sessionID", 10.5,
      15.67, "geometry", "profile", true, "device",
      "MockLocationEngine", 1300);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));

    NavigationNewData navigationNewData = new NavigationNewData(100, 750,
      "mewGeometry");
    NavigationRerouteData navigationRerouteData = new NavigationRerouteData(navigationNewData,12000);
    NavigationVoiceData navigationVoiceData = new NavigationVoiceData("voiceInstruction",
      TelemetryUtils.generateCreateDateFormatted(testDate));
    FeedbackData feedbackData = new FeedbackData("feedbackId");
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
    navigationState.setNavigationVoiceData(navigationVoiceData);
    navigationState.setFeedbackData(feedbackData);
    navigationState.setNavigationStepMetadata(navigationStepMetadata);

    NavigationRerouteEvent navigationRerouteEvent = new NavigationRerouteEvent(navigationState);
    GsonBuilder gsonBuilder = new GsonBuilder();

    JsonSerializer<NavigationRerouteEvent> serializer = new RerouteEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationRerouteEvent.class, serializer);

    Gson customGson = gsonBuilder.create();
    String payload =  customGson.toJson(navigationRerouteEvent);

    String expectedJson = "{\"absoluteDistanceToDestination\":1300,\"startTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,"
      + "\"durationRemaining\":180,\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdent\",\"sdkVersion\":\"sdkversion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":true,"
      + "\"device\":\"device\",\"locationEngine\":\"MockLocationEngine\",\"navigationNewData\":"
      + "{\"newDistanceRemaining\":100,\"newDurationRemaining\":750,\"newGeometry\":\"mewGeometry\"},"
      + "\"secondsSinceLastReroute\":12000,\"locationsBefore\":[null],\"locationsAfter\":[null],"
      + "\"feedbackId\":\"feedbackId\",\"screenshot\":\"screenshot\",\"voiceInstruction\":\"voiceInstruction\","
      + "\"voiceInstructionTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"step\":"
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
      180, "sdkIdent","sdkversion", "sessionID", 10.5,
      15.67, "geometry", "profile", true, "device",
      "MockLocationEngine", 1300);
    metadata.setCreated(TelemetryUtils.generateCreateDateFormatted(testDate));
    NavigationNewData navigationNewData = new NavigationNewData(100, 750,
      "mewGeometry");
    NavigationRerouteData navigationRerouteData = new NavigationRerouteData(navigationNewData,12000);

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
    String payload =  customGson.toJson(navigationFasterRouteEvent);

    String expectedJson = "{\"absoluteDistanceToDestination\":1300,\"startTimestamp\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"distanceCompleted\":13,\"distanceRemaining\":22,"
      + "\"durationRemaining\":180,\"operatingSystem\":\"Android - null\",\"eventVersion\":3,"
      + "\"sdKIdentifier\":\"sdkIdent\",\"sdkVersion\":\"sdkversion\",\"sessionIdentifier\":\"sessionID\","
      + "\"lat\":10.5,\"lng\":15.67,\"geometry\":\"geometry\",\"created\":\""
      + TelemetryUtils.generateCreateDateFormatted(testDate) + "\",\"profile\":\"profile\",\"simulation\":true,"
      + "\"device\":\"device\",\"locationEngine\":\"MockLocationEngine\",\"newDistanceRemaining\":100,"
      + "\"newDurationRemaining\":750,\"newGeometry\":\"mewGeometry\",\"step\":"
      + "{\"upcomingInstruction\":\"upcomingInstruction\",\"upcomingType\":\"upcomingType\","
      + "\"upcomingModifier\":\"upcomingModifier\",\"upcomingName\":\"upcomingName\",\"previousInstruction\":"
      + "\"previousInstruction\",\"previousType\":\"previousType\",\"previousModifier\":\"previousModifier\","
      + "\"previousName\":\"previousName\",\"distance\":100,\"duration\":1200,\"distanceRemaining\":250,"
      + "\"durationRemaining\":2200}}";

    assertEquals(expectedJson, payload);
  }
}