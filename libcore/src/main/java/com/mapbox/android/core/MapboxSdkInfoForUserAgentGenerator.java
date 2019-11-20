package com.mapbox.android.core;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Generator that reads(from assets/sdk_versions folder) and constructs Mapbox SDK versions for user agent.
 * Generates strings in format for each Mapbox library in the host app and concatenates them seperated by spaces.
 * <p> User agent format for Mapbox SDK : {SDK Name}/{Version} ({packageName}; {versionCode}) </p>
 */
public class MapboxSdkInfoForUserAgentGenerator {

  private static MapboxSdkInfoForUserAgentGenerator userAgentGenerator;

  private String sdkInfoForUserAgent;
  private static final Object lock = new Object();
  private static final Locale DEFAULT_LOCALE = Locale.US;
  private static final String USER_AGENT_SDK_VERSION_FORMAT = " %s (%s%s)";
  private static final String MAPBOX_IDENTIFIER = "mapbox";
  private static final String EMPTY_STRING = "";
  private static final String SDK_VERSIONS_FOLDER = "sdk_versions";
  private static final String LOG_TAG = "MapboxUAGenerator";

  private MapboxSdkInfoForUserAgentGenerator(AssetManager assetManager) {
    this.sdkInfoForUserAgent = getMapboxSdkIdentifiersForUserAgent(assetManager);
  }

  public static MapboxSdkInfoForUserAgentGenerator getInstance(@NonNull AssetManager assetManager) {
    if (userAgentGenerator == null) {
      synchronized (lock) {
        userAgentGenerator = new MapboxSdkInfoForUserAgentGenerator(assetManager);
      }
    }
    return userAgentGenerator;
  }

  @VisibleForTesting
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  String getMapboxSdkIdentifiersForUserAgent(@NonNull AssetManager assetManager) {
    StringBuilder stringBuilder = new StringBuilder(EMPTY_STRING);
    try {
      String[] files = assetManager.list(SDK_VERSIONS_FOLDER);
      if (files != null) {
        for (String fileName : files) {
          if (fileName.contains(MAPBOX_IDENTIFIER)) {
            InputStream inputStream = null;
            try {
              inputStream = assetManager.open(SDK_VERSIONS_FOLDER + File.separator + fileName);
              BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
              String nameAndVersion = reader.readLine();
              nameAndVersion = nameAndVersion != null ? nameAndVersion : EMPTY_STRING;
              StringBuilder sdkSubInfo = new StringBuilder(EMPTY_STRING);
              String subInfo;
              while ((subInfo = reader.readLine()) != null) {
                sdkSubInfo.append("; ");
                sdkSubInfo.append(subInfo);
              }
              reader.close();
              stringBuilder.append(String.format(DEFAULT_LOCALE, USER_AGENT_SDK_VERSION_FORMAT,
                nameAndVersion, fileName, sdkSubInfo.toString()));
            } catch (IOException exception) {
              Log.e(LOG_TAG, exception.toString());
            } finally {
              FileUtils.closeQuietly(inputStream);
            }
          }
        }
      }
    } catch (IOException exception) {
      Log.e(LOG_TAG, exception.toString());
    }
    return stringBuilder.toString().trim();
  }

  public String getSdkInfoForUserAgent() {
    return sdkInfoForUserAgent;
  }
}
