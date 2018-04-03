package com.mapbox.android.telemetry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

class RerouteEventSerializer implements JsonSerializer<NavigationRerouteEvent> {
  private static final String EVENT = "event";
  private static final String STEP = "step";

  @Override
  public JsonElement serialize(NavigationRerouteEvent src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject rerouteEvent = new JsonObject();
    rerouteEvent.addProperty(EVENT, src.getEvent());
    serializeMetadata(src, context, rerouteEvent);
    serializeRerouteData(src, context, rerouteEvent);
    serializeLocationData(src, context, rerouteEvent);
    serializeFeedbackData(src, context, rerouteEvent);
    serializeStep(src, context, rerouteEvent);
    return rerouteEvent;
  }

  private void serializeMetadata(NavigationRerouteEvent src, JsonSerializationContext context,
                                 JsonObject feedbackEvent) {
    JsonObject metadata = context.serialize(src.getNavigationMetadata()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : metadata.entrySet()) {
      feedbackEvent.add(e.getKey(), e.getValue());
    }
  }

  private void serializeRerouteData(NavigationRerouteEvent src, JsonSerializationContext context,
                                    JsonObject rerouteEvent) {
    JsonObject rerouteData = context.serialize(src.getNavigationRerouteData()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : rerouteData.entrySet()) {
      rerouteEvent.add(e.getKey(), e.getValue());
    }
  }

  private void serializeLocationData(NavigationRerouteEvent src, JsonSerializationContext context,
                                     JsonObject rerouteEvent) {
    JsonObject locationData = context.serialize(src.getNavigationLocationData()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : locationData.entrySet()) {
      rerouteEvent.add(e.getKey(), e.getValue());
    }
  }

  private void serializeFeedbackData(NavigationRerouteEvent src, JsonSerializationContext context,
                                     JsonObject feedbackEvent) {
    JsonObject feedbackData = context.serialize(src.getFeedbackData()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : feedbackData.entrySet()) {
      feedbackEvent.add(e.getKey(), e.getValue());
    }
  }

  private void serializeStep(NavigationRerouteEvent src, JsonSerializationContext context, JsonObject rerouteEvent) {
    JsonElement step = context.serialize(src.getStep());
    rerouteEvent.add(STEP, step);
  }
}
