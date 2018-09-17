package com.mapbox.android.telemetry;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class FeedbackEventDataSerializer implements JsonSerializer<FeedbackEventData> {
  private static final String FEEDBACK_TYPE = "feedbackType";
  private static final String DESCRIPTION = "description";
  private static final String SOURCE = "source";
  private static final String USER_ID = "userId";
  private static final String AUDIO = "audio";

  @Override
  public JsonElement serialize(FeedbackEventData src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject feedbackEventData = new JsonObject();
    feedbackEventData.addProperty(FEEDBACK_TYPE, src.getFeedbackType());
    feedbackEventData.addProperty(DESCRIPTION, src.getDescription());
    feedbackEventData.addProperty(SOURCE, src.getSource());
    feedbackEventData.addProperty(USER_ID, src.getUserId());
    return feedbackEventData;
  }
}
