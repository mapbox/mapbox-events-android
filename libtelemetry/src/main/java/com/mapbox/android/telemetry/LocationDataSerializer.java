package com.mapbox.android.telemetry;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class LocationDataSerializer implements JsonSerializer<NavigationLocationData> {
  private static final String LOCATIONS_BEFORE = "locationsBefore";
  private static final String LOCATIONS_AFTER = "locationsAfter";

  @Override
  public JsonElement serialize(NavigationLocationData src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject locationData = new JsonObject();
    JsonArray locationsBefore = context.serialize(src.getLocationsBefore()).getAsJsonArray();
    JsonArray locationsAfter = context.serialize(src.getLocationsAfter()).getAsJsonArray();
    locationData.add(LOCATIONS_BEFORE, locationsBefore);
    locationData.add(LOCATIONS_AFTER, locationsAfter);
    return locationData;
  }
}
