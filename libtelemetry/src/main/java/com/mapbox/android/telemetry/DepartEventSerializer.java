package com.mapbox.android.telemetry;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class DepartEventSerializer implements JsonSerializer<NavigationDepartEvent> {
  private static final String EVENT = "event";

  @Override
  public JsonElement serialize(NavigationDepartEvent src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject departEvent = context.serialize(src.getMetadata()).getAsJsonObject();
    departEvent.addProperty(EVENT, src.getEvent());
    return departEvent;
  }
}
