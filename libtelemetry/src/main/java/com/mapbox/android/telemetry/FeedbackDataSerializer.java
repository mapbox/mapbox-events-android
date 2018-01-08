package com.mapbox.android.telemetry;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class FeedbackDataSerializer implements JsonSerializer<FeedbackData> {
  private static final String FEEDBACK_ID = "feedbackId";
  private static final String SCREENSHOT = "screenshot";

  @Override
  public JsonElement serialize(FeedbackData src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject feedbackData = new JsonObject();
    feedbackData.addProperty(FEEDBACK_ID, src.getFeedbackId());
    feedbackData.addProperty(SCREENSHOT, src.getScreenshot());
    return feedbackData;
  }
}
