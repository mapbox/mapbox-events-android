package com.mapbox.android.telemetry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

class FasterRouteEventSerializer implements JsonSerializer<NavigationFasterRouteEvent> {
  private static final String EVENT = "event";
  private static final String STEP = "step";

  @Override
  public JsonElement serialize(NavigationFasterRouteEvent src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject fasterRoute = new JsonObject();
    fasterRoute.addProperty(EVENT, src.getEvent());
    serializeMetadata(src, context, fasterRoute);
    serializeNewData(src, context, fasterRoute);
    serializeStep(src, context, fasterRoute);
    return fasterRoute;
  }

  private void serializeMetadata(NavigationFasterRouteEvent src, JsonSerializationContext context,
                                 JsonObject fasterRoute) {
    JsonObject metadata = context.serialize(src.getMetadata()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : metadata.entrySet()) {
      fasterRoute.add(e.getKey(), e.getValue());
    }
  }

  private void serializeNewData(NavigationFasterRouteEvent src, JsonSerializationContext context,
                                JsonObject fasterRoute) {
    JsonObject newData = context.serialize(src.getNavigationNewData()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : newData.entrySet()) {
      fasterRoute.add(e.getKey(), e.getValue());
    }
  }

  private void serializeStep(NavigationFasterRouteEvent src, JsonSerializationContext context, JsonObject fasterRoute) {
    JsonElement step = context.serialize(src.getStep());
    fasterRoute.add(STEP, step);
  }
}
