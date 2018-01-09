package com.mapbox.android.telemetry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class NewDataSerializer implements JsonSerializer<NavigationNewData> {
  private static final String NEW_DISTANCE_REMAINING = "newDistanceRemaining";
  private static final String NEW_DURATION_REMAINING = "newDurationRemaining";
  private static final String NEW_GEOMETRY = "newGeometry";

  @Override
  public JsonElement serialize(NavigationNewData src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject newData = new JsonObject();
    newData.addProperty(NEW_DISTANCE_REMAINING, src.getNewDistanceRemaining());
    newData.addProperty(NEW_DURATION_REMAINING, src.getNewDurationRemaining());
    newData.addProperty(NEW_GEOMETRY, src.getNewGeometry());
    return newData;
  }
}