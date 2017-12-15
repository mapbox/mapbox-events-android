package com.mapbox.services.android.telemetry;

import android.content.Context;
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
}
