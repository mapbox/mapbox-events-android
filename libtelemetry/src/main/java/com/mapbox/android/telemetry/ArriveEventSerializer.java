package com.mapbox.android.telemetry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

class ArriveEventSerializer implements JsonSerializer<NavigationArriveEvent> {
  private static final String EVENT = "event";

  @Override
  public JsonElement serialize(NavigationArriveEvent src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject arriveEvent = new JsonObject();
    arriveEvent.addProperty(EVENT, src.getEvent());
    serializeMetadata(src, context, arriveEvent);
    return arriveEvent;
  }

  private void serializeMetadata(NavigationArriveEvent src, JsonSerializationContext context,
                                 JsonObject arriveEvent) {
    JsonObject metadata = context.serialize(src.getMetadata()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : metadata.entrySet()) {
      arriveEvent.add(e.getKey(), e.getValue());
    }
  }
}
