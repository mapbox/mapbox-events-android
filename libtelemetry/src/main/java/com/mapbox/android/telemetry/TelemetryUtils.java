package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okhttp3.internal.Util;

class TelemetryUtils {
  private static final String DATE_AND_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  private static final String EMPTY_STRING = "";
  private static final String TWO_STRING_FORMAT = "%s%s";
  private static final String THREE_STRING_FORMAT = "%s/%s/%s";
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_AND_TIME_PATTERN, Locale.US);
  private static final Locale DEFAULT_LOCALE = Locale.US;
  private static final String MAPBOX_SHARED_PREFERENCES = "MapboxSharedPreferences";
  private static final String MAPBOX_SHARED_PREFERENCE_KEY_ENABLED_TELEMETRY = "mapboxEnabledTelemetry";

  static String obtainCurrentDate() {
    return dateFormat.format(new Date());
  }

  static String generateCreateDateFormatted(Date date) {
    return dateFormat.format(date);
  }

  static String obtainUniversalUniqueIdentifier() {
    String universalUniqueIdentifier = UUID.randomUUID().toString();
    return universalUniqueIdentifier;
  }

  static String createFullUserAgent(String userAgent, Context context) {
    String appIdentifier = TelemetryUtils.obtainApplicationIdentifier(context);
    String newUserAgent = Util.toHumanReadableAscii(String.format(DEFAULT_LOCALE, TWO_STRING_FORMAT, appIdentifier,
      userAgent));
    String fullUserAgent = TextUtils.isEmpty(appIdentifier) ? userAgent : newUserAgent;

    return fullUserAgent;
  }

  static boolean isEmpty(String string) {
    if (string == null || string.length() == 0) {
      return true;
    } else {
      return false;
    }
  }

  static boolean updateEnabledTelemetry(boolean enabledTelemetry) {
    SharedPreferences sharedPreferences = obtainSharedPreferences();
    SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putBoolean(MAPBOX_SHARED_PREFERENCE_KEY_ENABLED_TELEMETRY, enabledTelemetry);
    editor.apply();

    return enabledTelemetry;
  }

  static boolean retrieveEnabledTelemetry() {
    SharedPreferences sharedPreferences = obtainSharedPreferences();
    Boolean enabledTelemetry = sharedPreferences.getBoolean(MAPBOX_SHARED_PREFERENCE_KEY_ENABLED_TELEMETRY, false);

    return enabledTelemetry;
  }

  private static String obtainApplicationIdentifier(Context context) {
    try {
      String packageName = context.getPackageName();
      PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
      String appIdentifier = String.format(DEFAULT_LOCALE, THREE_STRING_FORMAT, packageName,
        packageInfo.versionName, packageInfo.versionCode);

      return appIdentifier;
    } catch (Exception exception) {
      return EMPTY_STRING;
    }
  }

  private static SharedPreferences obtainSharedPreferences() {
    return MapboxTelemetry.applicationContext.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE);
  }
}
