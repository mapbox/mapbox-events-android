package com.mapbox.android.telemetry;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class NavigationMetadataSerializer implements JsonSerializer<NavigationMetadata> {
  private static final String CREATED = "created";
  private static final String ABSOLUTE_DISTANCE_TO_DESTINATION = "absoluteDistanceToDestination";
  private static final String PERCENT_TIME_IN_PORTRAIT = "percentTimeInPortrait";
  private static final String PERCENT_TIME_IN_FOREGROUND = "percentTimeInForeground";
  private static final String START_TIMESTAMP = "startTimestamp";
  private static final String DISTANCE_COMPLETED = "distanceCompleted";
  private static final String DISTANCE_REMAINING = "distanceRemaining";
  private static final String DURATION_REMAINING = "durationRemaining";
  private static final String OPERATING_SYSTEM = "operatingSystem";
  private static final String EVENT_VERSION = "eventVersion";
  private static final String SDK_IDENTIFIER = "sdKIdentifier";
  private static final String SDK_VERSION = "sdkVersion";
  private static final String SESSION_IDENTIFIER = "sessionIdentifier";
  private static final String LATITUDE = "lat";
  private static final String LONGITUDE = "lng";
  private static final String GEOMETRY = "geometry";
  private static final String PROFILE = "profile";
  private static final String ESTIMATED_DISTANCE = "estimatedDistance";
  private static final String ESTIMATED_DURATION = "estimatedDuration";
  private static final String REROUTE_COUNT = "rerouteCount";
  private static final String SIMULATION = "simulation";
  private static final String ORIGINAL_REQUEST_IDENTIFIER = "originalRequestIdentifier";
  private static final String REQUEST_IDENTIFIER = "requestIdentifier";
  private static final String ORIGINAL_GEOMETRY = "originalGeometry";
  private static final String ORIGINAL_ESTIMATED_DISTANCE = "originalEstimatedDistance";
  private static final String ORIGINAL_ESTIMATED_DURATION = "originalEstimatedDuration";
  private static final String AUDIO_TYPE = "audioType";
  private static final String ORIGINAL_STEP_COUNT = "originalStepCount";
  private static final String DEVICE = "device";
  private static final String LOCATION_ENGINE = "locationEngine";
  private static final String VOLUME_LEVEL = "volumeLevel";
  private static final String SCREEN_BRIGHTNESS = "screenBrightness";
  private static final String APPLICATION_STATE = "applicationState";
  private static final String BATTERY_PLUGGED_IN = "batteryPluggedIn";
  private static final String BATTERY_LEVEL = "batteryLevel";
  private static final String CONNECTIVITY = "connectivity";
  private static final String TRIP_IDENTIFIER = "tripIdentifier";
  private static final String LEG_INDEX = "legIndex";
  private static final String LEG_COUNT = "legCount";
  private static final String STEP_INDEX = "stepIndex";
  private static final String STEP_COUNT = "stepCount";
  private static final String VOICE_INDEX = "voiceIndex";
  private static final String BANNER_INDEX = "bannerIndex";
  private static final String TOTAL_STEP_COUNT = "totalStepCount";

  @Override
  public JsonElement serialize(NavigationMetadata src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject metadata = new JsonObject();
    serializeRequired(src, metadata);
    serializeOptional(src, metadata);
    return metadata;
  }

  private void serializeRequired(NavigationMetadata src, JsonObject metadata) {
    metadata.addProperty(START_TIMESTAMP, src.getStartTimestamp());
    metadata.addProperty(DISTANCE_COMPLETED, src.getDistanceCompleted());
    metadata.addProperty(DISTANCE_REMAINING, src.getDistanceRemaining());
    metadata.addProperty(DURATION_REMAINING, src.getDurationRemaining());
    metadata.addProperty(OPERATING_SYSTEM, src.getOperatingSystem());
    metadata.addProperty(EVENT_VERSION, src.getEventVersion());
    metadata.addProperty(SDK_IDENTIFIER, src.getSdKIdentifier());
    metadata.addProperty(SDK_VERSION, src.getSdkVersion());
    metadata.addProperty(SESSION_IDENTIFIER, src.getSessionIdentifier());
    metadata.addProperty(LATITUDE, src.getLat());
    metadata.addProperty(LONGITUDE, src.getLng());
    metadata.addProperty(GEOMETRY, src.getGeometry());
    metadata.addProperty(PROFILE, src.getProfile());
    metadata.addProperty(SIMULATION, src.isSimulation());
    metadata.addProperty(DEVICE, src.getDevice());
    metadata.addProperty(LOCATION_ENGINE, src.getLocationEngine());
    metadata.addProperty(CREATED, src.getCreated());
    metadata.addProperty(ABSOLUTE_DISTANCE_TO_DESTINATION, src.getAbsoluteDistanceToDestination());
    metadata.addProperty(TRIP_IDENTIFIER, src.getTripIdentifier());
    metadata.addProperty(LEG_INDEX, src.getLegIndex());
    metadata.addProperty(LEG_COUNT, src.getLegCount());
    metadata.addProperty(STEP_INDEX, src.getStepIndex());
    metadata.addProperty(STEP_COUNT, src.getStepCount());
    metadata.addProperty(TOTAL_STEP_COUNT, src.getTotalStepCount());
  }

  private void serializeOptional(NavigationMetadata src, JsonObject metadata) {
    metadata.addProperty(ESTIMATED_DISTANCE, src.getEstimatedDistance());
    metadata.addProperty(ESTIMATED_DURATION, src.getEstimatedDuration());
    metadata.addProperty(REROUTE_COUNT, src.getRerouteCount());
    metadata.addProperty(ORIGINAL_REQUEST_IDENTIFIER, src.getOriginalRequestIdentifier());
    metadata.addProperty(REQUEST_IDENTIFIER, src.getRequestIdentifier());
    metadata.addProperty(ORIGINAL_GEOMETRY, src.getOriginalGeometry());
    metadata.addProperty(ORIGINAL_ESTIMATED_DISTANCE, src.getOriginalEstimatedDistance());
    metadata.addProperty(ORIGINAL_ESTIMATED_DURATION, src.getOriginalEstimatedDuration());
    metadata.addProperty(AUDIO_TYPE, src.getAudioType());
    metadata.addProperty(ORIGINAL_STEP_COUNT, src.getOriginalStepCount());
    metadata.addProperty(VOLUME_LEVEL, src.getVolumeLevel());
    metadata.addProperty(SCREEN_BRIGHTNESS, src.getScreenBrightness());
    metadata.addProperty(APPLICATION_STATE, src.getApplicationState());
    metadata.addProperty(BATTERY_PLUGGED_IN, src.isBatteryPluggedIn());
    metadata.addProperty(BATTERY_LEVEL, src.getBatteryLevel());
    metadata.addProperty(CONNECTIVITY, src.getConnectivity());
    metadata.addProperty(PERCENT_TIME_IN_PORTRAIT, src.getPercentTimeInPortrait());
    metadata.addProperty(PERCENT_TIME_IN_FOREGROUND, src.getPercentTimeInForeground());
    metadata.addProperty(VOICE_INDEX, src.getVoiceIndex());
    metadata.addProperty(BANNER_INDEX, src.getBannerIndex());
  }
}
