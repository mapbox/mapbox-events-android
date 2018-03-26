package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okio.Buffer;

public class TelemetryUtils {
  static final String MAPBOX_SHARED_PREFERENCES = "MapboxSharedPreferences";
  static final String MAPBOX_SHARED_PREFERENCE_KEY_VENDOR_ID = "mapboxVendorId";
  private static final String DATE_AND_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  private static final String EMPTY_STRING = "";
  private static final String TWO_STRING_FORMAT = "%s %s";
  private static final String THREE_STRING_FORMAT = "%s/%s/%s";
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_AND_TIME_PATTERN, Locale.US);
  private static final Locale DEFAULT_LOCALE = Locale.US;

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
    String newUserAgent = toHumanReadableAscii(String.format(DEFAULT_LOCALE, TWO_STRING_FORMAT, appIdentifier,
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

  static String retrieveVendorId() {
    SharedPreferences sharedPreferences = obtainSharedPreferences();
    String mapboxVendorId = sharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_VENDOR_ID, "");

    if (TelemetryUtils.isEmpty(mapboxVendorId)) {
      mapboxVendorId = TelemetryUtils.updateVendorId();
    }

    return mapboxVendorId;
  }

  static SharedPreferences obtainSharedPreferences() {
    return MapboxTelemetry.applicationContext.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE);
  }

  private static String updateVendorId() {
    SharedPreferences sharedPreferences = obtainSharedPreferences();
    SharedPreferences.Editor editor = sharedPreferences.edit();

    String uniqueId = obtainUniversalUniqueIdentifier();
    editor.putString(MAPBOX_SHARED_PREFERENCE_KEY_VENDOR_ID, uniqueId);
    editor.apply();

    return uniqueId;
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

  public static String toHumanReadableAscii(String s) {
    for (int i = 0, length = s.length(), c; i < length; i += Character.charCount(c)) {
      c = s.codePointAt(i);
      if (c > '\u001f' && c < '\u007f') {
        continue;
      }

      Buffer buffer = new Buffer();
      buffer.writeUtf8(s, 0, i);
      for (int j = i; j < length; j += Character.charCount(c)) {
        c = s.codePointAt(j);
        buffer.writeUtf8CodePoint(c > '\u001f' && c < '\u007f' ? c : '?');
      }
      return buffer.readUtf8();
    }
    return s;
  }
}
