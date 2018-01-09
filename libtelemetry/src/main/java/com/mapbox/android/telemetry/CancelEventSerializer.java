package com.mapbox.android.telemetry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

class CancelEventSerializer implements JsonSerializer<NavigationCancelEvent> {
  private static final String EVENT = "event";

  public JsonElement serialize(NavigationCancelEvent src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject cancelEvent = new JsonObject();
    cancelEvent.addProperty(EVENT, src.getEvent());
    serializeCancelData(src, context, cancelEvent);
    serializeMetadata(src, context, cancelEvent);
    return cancelEvent;
  }

  private void serializeCancelData(NavigationCancelEvent src, JsonSerializationContext context,
                                   JsonObject cancelEvent) {
    JsonObject cancelData = context.serialize(src.getCancelData()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : cancelData.entrySet()) {
      cancelEvent.add(e.getKey(), e.getValue());
    }
  }

  private void serializeMetadata(NavigationCancelEvent src, JsonSerializationContext context,
                                 JsonObject cancelEvent) {
    JsonObject metadata = context.serialize(src.getMetadata()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : metadata.entrySet()) {
      cancelEvent.add(e.getKey(), e.getValue());
    }
  }
}
