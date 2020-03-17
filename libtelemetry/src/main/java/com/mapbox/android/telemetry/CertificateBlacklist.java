package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class should be instantiated on a background thread,
 * due to few i/o operations and access to shared preferences.
 */
class CertificateBlacklist implements ConfigurationChangeHandler {
  private static final String LOG_TAG = "MapboxBlacklist";
  private static final String BLACKLIST_FILE = "MapboxBlacklist";
  private final Context context;
  private final List<String> revokedKeys;

  CertificateBlacklist(Context context, ConfigurationClient configurationClient) {
    this.context = context;
    this.revokedKeys = new CopyOnWriteArrayList<>();
    attemptCleanUp();
    configurationClient.addHandler(this);
    // Check if it's time to update
    if (configurationClient.shouldUpdate()) {
      configurationClient.update();
    } else {
      retrieveBlackList(false);
    }
  }

  boolean isBlacklisted(String hash) {
    return revokedKeys.contains(hash);
  }

  boolean attemptCleanUp() {
    boolean isDeleted = false;
    try {
      File path = context.getFilesDir();
      if (!path.isDirectory()) {
        return false;
      }

      File file = new File(path, BLACKLIST_FILE);
      if (file.exists()) {
        isDeleted = file.delete();
      }
    } catch (Exception exception) {
      Log.d(LOG_TAG, "Error deleting file!");
    }

    return isDeleted;
  }

  @VisibleForTesting
  void retrieveBlackList(boolean overwrite) {
    List<String> blacklist = new ArrayList<>();
    try {
      SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
      String configurationString = sharedPreferences.getString(MapboxTelemetryConstants.MAPBOX_CONFIGURATION, null);

      if (configurationString != null) {
        Configuration configuration = new Gson().fromJson(configurationString, Configuration.class);
        blacklist = Arrays.asList(configuration.getCertificateBlacklists());
      }

      if (blacklist.isEmpty()) {
        return;
      }

      if (overwrite) {
        revokedKeys.clear();
      }
      revokedKeys.addAll(blacklist);
    } catch (Exception exception) {
      Log.e(LOG_TAG, exception.getMessage());
    }
  }

  @Override
  public void onUpdate() {
    // This callback is dispatched on background thread
    retrieveBlackList(true);
  }
}