package com.mapbox.android.telemetry;

import android.content.Context;
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

class CertificateBlacklist implements ConfigurationChangeHandler {
  private static final String LOG_TAG = "MapboxBlacklist";
  private static final String BLACKLIST_FILE = "MapboxBlacklist";
  private final Context context;
  private List<String> revokedKeys;

  CertificateBlacklist(Context context, ConfigurationClient configurationClient) {
    this.context = context;
    configurationClient.addHandler(this);

    // Check if it's time to update
    if (configurationClient.shouldUpdate()) {
      configurationClient.update();
    } else {
      retrieveBlackList(context.getFilesDir());
    }
  }

  boolean isBlacklisted(String hash) {
    return revokedKeys.contains(hash);
  }

  private void retrieveBlackList(File directory) {
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

  private void saveBlackList(String data) {
    Gson gson = new GsonBuilder().create();
    JsonObject responseJson = new JsonObject();

    try {
      responseJson = gson.fromJson(data, JsonObject.class);
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

  @Override
  public void onUpdate(String data) {
    saveBlackList(data);
    retrieveBlackList(context.getFilesDir());
  }
}
