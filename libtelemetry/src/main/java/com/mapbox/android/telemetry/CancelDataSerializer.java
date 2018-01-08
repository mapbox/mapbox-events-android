package com.mapbox.android.telemetry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class CancelDataSerializer implements JsonSerializer<NavigationCancelData> {
  private static final String COMMENT = "comment";
  private static final String RATING = "rating";
  private static final String ARRIVAL_TIMESTAMP = "arrivalTimestamp";

  @Override
  public JsonElement serialize(NavigationCancelData src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject cancelData = new JsonObject();
    cancelData.addProperty(COMMENT, src.getComment());
    cancelData.addProperty(RATING, src.getRating());
    cancelData.addProperty(ARRIVAL_TIMESTAMP, src.getArrivalTimestamp());
    return cancelData;
  }
}
