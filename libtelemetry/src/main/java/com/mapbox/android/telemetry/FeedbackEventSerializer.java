package com.mapbox.android.telemetry;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

class FeedbackEventSerializer implements JsonSerializer<NavigationFeedbackEvent> {
  private static final String EVENT = "event";
  private static final String STEP = "step";

  @Override
  public JsonElement serialize(NavigationFeedbackEvent src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject feedbackEvent = new JsonObject();
    feedbackEvent.addProperty(EVENT, src.getEvent());
    serializeMetadata(src, context, feedbackEvent);
    serializeFeedbackEventData(src, context, feedbackEvent);
    serializeLocationData(src, context, feedbackEvent);
    serializeFeedbackData(src, context, feedbackEvent);
    serializeStep(src, context, feedbackEvent);
    return feedbackEvent;
  }

  private void serializeMetadata(NavigationFeedbackEvent src, JsonSerializationContext context,
                                 JsonObject feedbackEvent) {
    JsonObject metadata = context.serialize(src.getMetadata()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : metadata.entrySet()) {
      feedbackEvent.add(e.getKey(), e.getValue());
    }
  }

  private void serializeFeedbackEventData(NavigationFeedbackEvent src, JsonSerializationContext context,
                                          JsonObject feedbackEvent) {
    JsonObject feedbackEventData = context.serialize(src.getFeedbackEventData()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : feedbackEventData.entrySet()) {
      feedbackEvent.add(e.getKey(), e.getValue());
    }
  }

  private void serializeLocationData(NavigationFeedbackEvent src, JsonSerializationContext context,
                                     JsonObject feedbackEvent) {
    JsonObject locationData = context.serialize(src.getNavigationLocationData()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : locationData.entrySet()) {
      feedbackEvent.add(e.getKey(), e.getValue());
    }
  }

  private void serializeFeedbackData(NavigationFeedbackEvent src, JsonSerializationContext context,
                                     JsonObject feedbackEvent) {
    JsonObject feedbackData = context.serialize(src.getFeedbackData()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : feedbackData.entrySet()) {
      feedbackEvent.add(e.getKey(), e.getValue());
    }
  }

  private void serializeStep(NavigationFeedbackEvent src, JsonSerializationContext context, JsonObject feedbackEvent) {
    JsonElement step = context.serialize(src.getStep());
    feedbackEvent.add(STEP, step);
  }
}
