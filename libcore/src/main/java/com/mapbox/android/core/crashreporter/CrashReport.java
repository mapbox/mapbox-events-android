package com.mapbox.android.core.crashreporter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CrashReport {
  private static final String TAG = "MapboxCrashReport";
  private static final String CRASH_EVENT = "mobile.crash";
  private final JSONObject content;

  CrashReport(Calendar created) {
    this.content = new JSONObject();
    put("event", CRASH_EVENT);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    put("created", dateFormat.format(created));
  }

  CrashReport(String json) throws JSONException {
    this.content = new JSONObject(json);
  }

  public synchronized void put(@NonNull String key, @Nullable String value) {
    if (value == null) {
      putNull(key);
      return;
    }

    try {
      this.content.put(key, value);
    } catch (JSONException je) {
      Log.e(TAG, "Failed json encode value: " + String.valueOf(value));
    }
  }

  @NonNull
  public String getDateString() {
    return getString("created");
  }

  @NonNull
  public String toJson() {
    return this.content.toString();
  }

  @NonNull
  private String getString(@NonNull String key) {
    return this.content.optString(key);
  }

  private void putNull(@NonNull String key) {
    try {
      this.content.put(key, "null");
    } catch (JSONException je) {
      Log.e(TAG, "Failed json encode null value");
    }
  }
}
