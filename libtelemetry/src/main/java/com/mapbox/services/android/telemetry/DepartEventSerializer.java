package com.mapbox.services.android.telemetry;


import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class DepartEventSerializer implements JsonSerializer<NavigationDepartEvent> {

  @Override
  public JsonElement serialize(NavigationDepartEvent src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src.getMetadata());
  }
}
