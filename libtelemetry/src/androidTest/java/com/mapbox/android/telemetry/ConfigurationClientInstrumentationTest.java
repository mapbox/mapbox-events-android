package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ConfigurationClientInstrumentationTest {
  private ConfigurationClient configurationClient;
  private static final long DAY_IN_MILLIS = 86400000;

  @Before
  public void setup() {
    Context context = InstrumentationRegistry.getTargetContext();
    this.configurationClient = new ConfigurationClient(context,
      TelemetryUtils.createFullUserAgent("AnUserAgent", context), "anAccessToken", new OkHttpClient());
  }

  @Test
  public void shouldUpdateTest() {
    setTimeStamp(System.currentTimeMillis() - DAY_IN_MILLIS);
    assertTrue(configurationClient.shouldUpdate());

    setTimeStamp(System.currentTimeMillis());
    assertFalse(configurationClient.shouldUpdate());
  }

  @Test
  public void saveTimeStampTest() {
    setTimeStamp(System.currentTimeMillis() - DAY_IN_MILLIS);
    Call mockedCall = mock(Call.class);
    IOException mockedException = mock(IOException.class);

    configurationClient.onFailure(mockedCall, mockedException);

    assertFalse(configurationClient.shouldUpdate());
  }

  @Test
  public void updateRequestTest() {
    setTimeStamp(System.currentTimeMillis() - DAY_IN_MILLIS);

    ConfigurationChangeHandler configurationChangeHandler = new ConfigurationChangeHandler() {
      @Override
      public void onUpdate(String data) {
        assertTrue(isValidContent(data));
      }
    };

    configurationClient.addHandler(configurationChangeHandler);
    configurationClient.update();
  }

  private void setTimeStamp(long milliseconds) {
    SharedPreferences sharedPreferences =
      TelemetryUtils.obtainSharedPreferences(InstrumentationRegistry.getTargetContext());
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putLong("mapboxConfigSyncTimestamp", milliseconds);
    editor.apply();
  }

  private static boolean isValidContent(String data) {
    Gson gson = new GsonBuilder().create();
    try {
      JsonObject responseJson = gson.fromJson(data, JsonObject.class);
      JsonElement jsonElement = responseJson.get("RevokedCertKeys");

      return jsonElement.isJsonArray();
    } catch (JsonSyntaxException exception) {
      return false;
    }
  }
}
