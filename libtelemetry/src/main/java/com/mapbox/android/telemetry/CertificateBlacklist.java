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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class CertificateBlacklist implements ConfigurationChangeHandler {
  private static final String LOG_TAG = "MapboxBlacklist";
  private static final String BLACKLIST_FILE = "MapboxBlacklist";
  private final Context context;
  private final List<String> revokedKeys;

  CertificateBlacklist(Context context, ConfigurationClient configurationClient) {
    this.context = context;
    this.revokedKeys = new CopyOnWriteArrayList<>();
    configurationClient.addHandler(this);

    // Check if it's time to update
    if (configurationClient.shouldUpdate()) {
      configurationClient.update();
    } else {
      retrieveBlackList(context.getFilesDir(), false);
    }
  }

  boolean isBlacklisted(String hash) {
    return revokedKeys.contains(hash);
  }

  private void retrieveBlackList(File path, boolean overwrite) {
    if (!path.isDirectory()) {
      return;
    }

    File file = new File(path, BLACKLIST_FILE);
    if (file.exists()) {
      try {
        List<String> blacklist = obtainBlacklistContents(file);
        if (blacklist.isEmpty()) {
          return;
        }

        if (overwrite) {
          revokedKeys.clear();
        }
        revokedKeys.addAll(blacklist);
      } catch (IOException exception) {
        Log.e(LOG_TAG, exception.getMessage());
      }
    }
  }

  private boolean saveBlackList(String data) {
    if (!isValidContent(data)) {
      return false;
    }

    boolean success = true;
    FileOutputStream outputStream = null;
    try {
      outputStream = context.openFileOutput(BLACKLIST_FILE, Context.MODE_PRIVATE);
      outputStream.write(data.getBytes());
    } catch (IOException exception) {
      Log.e(LOG_TAG, exception.getMessage());
      success = false;
    } finally {
      try {
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (IOException exception) {
        Log.e(LOG_TAG, exception.getMessage());
        success = false;
      }
    }
    return success;
  }

  private static boolean isValidContent(String data) {
    Gson gson = new GsonBuilder().create();
    JsonArray jsonArray;
    try {
      JsonObject responseJson = gson.fromJson(data, JsonObject.class);
      jsonArray = responseJson.getAsJsonArray("RevokedCertKeys");
    } catch (JsonSyntaxException exception) {
      Log.e(LOG_TAG, exception.getMessage());
      return false;
    }
    return jsonArray != null && jsonArray.size() > 0;
  }

  private List<String> obtainBlacklistContents(File file) throws IOException {
    InputStream inputStream = new FileInputStream(file);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    Gson gson = new Gson();

    List<String> blacklist = null;
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
    return blacklist != null ? blacklist : Collections.<String>emptyList();
  }

  @Override
  public void onUpdate(String data) {
    if (saveBlackList(data)) {
      retrieveBlackList(context.getFilesDir(), true);
    }
  }
}
