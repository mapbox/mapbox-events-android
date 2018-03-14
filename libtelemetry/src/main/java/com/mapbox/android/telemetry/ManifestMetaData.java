package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

class ManifestMetaData {
  private static final String KEY_META_DATA_DISABLE = "com.mapbox.DisableEvents";

  static boolean obtainDisableEventsTag(Context context) {
    try {
      ApplicationInfo appInformation = context.getPackageManager().getApplicationInfo(
        context.getPackageName(), PackageManager.GET_META_DATA);

      if (appInformation != null && appInformation.metaData != null) {
        boolean disableBool = appInformation.metaData.getBoolean(KEY_META_DATA_DISABLE);
        return disableBool;
      }
    } catch (PackageManager.NameNotFoundException exception) {
      exception.printStackTrace();
    }

    return false;
  }
}