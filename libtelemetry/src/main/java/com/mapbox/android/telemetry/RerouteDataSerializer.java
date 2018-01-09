package com.mapbox.android.telemetry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

class RerouteDataSerializer implements JsonSerializer<NavigationRerouteData> {
  private static final String SECOND_SINCE_LAST_REROUTE = "secondsSinceLastReroute";

  @Override
  public JsonElement serialize(NavigationRerouteData src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject rerouteData = new JsonObject();
    rerouteData.addProperty(SECOND_SINCE_LAST_REROUTE, src.getSecondsSinceLastReroute());
    serializeNewData(src, context, rerouteData);
    return rerouteData;
  }

  private void serializeNewData(NavigationRerouteData src, JsonSerializationContext context,
                                JsonObject rerouteData) {
    JsonObject newData = context.serialize(src.getNavigationNewData()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> e : newData.entrySet()) {
      rerouteData.add(e.getKey(), e.getValue());
    }
  }
}
