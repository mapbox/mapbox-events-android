package com.mapbox.android.telemetry.errors;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mapbox.android.telemetry.CrashEvent;

public final class ErrorUtils {
  private static final String LOG_TAG = "ErrorUtils";

  public static CrashEvent parseJsonCrashEvent(String json) {
    Gson gson = new GsonBuilder().create();
    try {
      return gson.fromJson(json, CrashEvent.class);
    } catch (JsonSyntaxException jse) {
      Log.e(LOG_TAG, jse.toString());
      return new CrashEvent(null, null);
    }
  }
}
