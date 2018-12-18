package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

class CertificateBlacklist implements Callback {
  private static final String LOG_TAG = "MapboxBlacklist";
  private static final String BLACKLIST_FILE = "MapboxBlacklist";
  private static final long DAY_IN_MILLIS = 86400000;
  private static final String REQUEST_FAIL = "Request failed to download blacklist";
  static final String MAPBOX_SHARED_PREFERENCE_KEY_BLACKLIST_TIMESTAMP = "mapboxBlacklistTimestamp";
  private final Context context;
  private final String accessToken;
  private  final BlacklistClient blacklistClient;
  private List<String> revokedKeys;

  CertificateBlacklist(Context context, String accessToken, String userAgent) {
    this.context = context;
    this.accessToken = accessToken;
    this.blacklistClient = new BlacklistClient(userAgent, new OkHttpClient(), this);
    retrieveBlackList();
  }

  boolean isBlacklisted(String hash) {
    for (String revokedKey : revokedKeys) {
      if (hash.contains(revokedKey)) {
        return true;
      }
    }
    return false;
  }

  private void retrieveBlackList() {
    File directory = context.getFilesDir();
    revokedKeys = new ArrayList<>();

    if (directory.isDirectory()) {
      File file = new File(directory, BLACKLIST_FILE);

      if (file.exists()) {
        try {
          revokedKeys = obtainBlacklistContents(file);
        } catch (IOException exception) {
          Log.e(LOG_TAG, exception.getMessage());
        }
      }
    }
  }

  boolean daySinceLastUpdate() {
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    long lastUpdateTime = sharedPreferences.getLong(MAPBOX_SHARED_PREFERENCE_KEY_BLACKLIST_TIMESTAMP, 0);

    long millisecondDiff = System.currentTimeMillis() - lastUpdateTime;
    return millisecondDiff >= DAY_IN_MILLIS;
  }

  void requestBlacklist() {
    blacklistClient.requestBlacklist(blacklistClient.generateRequestUrl(context, accessToken));
  }

  private void saveBlackList(ResponseBody responseBody) throws IOException {
    if (responseBody == null) {
      return;
    }

    Gson gson = new GsonBuilder().create();
    JsonObject responseJson = new JsonObject();

    try {
      responseJson = gson.fromJson(responseBody.string(), JsonObject.class);
    } catch (JsonSyntaxException exception) {
      Log.e(LOG_TAG, exception.getMessage());
    }

    JsonArray jsonArray = responseJson.getAsJsonArray("RevokedCertKeys");
    Type listType = new TypeToken<List<String>>(){}.getType();
    revokedKeys = gson.fromJson(jsonArray.toString(),listType);
    FileOutputStream outputStream = null;

    try {
      outputStream = context.openFileOutput(BLACKLIST_FILE, Context.MODE_PRIVATE);
      outputStream.write(responseJson.toString().getBytes());
    } catch (IOException exception) {
      Log.e(LOG_TAG, exception.getMessage());
    } finally {
      try {
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (IOException exception) {
        Log.e(LOG_TAG, exception.getMessage());
      }
    }
  }

  @Override
  public void onFailure(Call call, IOException exception) {
    Log.e(LOG_TAG, REQUEST_FAIL, exception);
    saveTimestamp();
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    if (response.body() != null) {
      saveBlackList(response.body());
    }
    saveTimestamp();
  }

  private List<String> obtainBlacklistContents(File file) throws IOException {
    InputStream inputStream = new FileInputStream(file);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    List<String> blacklist = new ArrayList<>();
    Gson gson = new Gson();

    try {
      JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
      if (jsonObject != null) {
        JsonArray jsonArray = jsonObject.getAsJsonArray("RevokedCertKeys");

        Type listType = new TypeToken<List<String>>(){}.getType();
        blacklist = gson.fromJson(jsonArray.toString(),listType);
      }

    } catch (JsonIOException | JsonSyntaxException exception) {
      Log.e(LOG_TAG, exception.getMessage());
    }

    return blacklist;
  }

  private void saveTimestamp() {
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putLong(MAPBOX_SHARED_PREFERENCE_KEY_BLACKLIST_TIMESTAMP, System.currentTimeMillis());
    editor.apply();
  }
}
