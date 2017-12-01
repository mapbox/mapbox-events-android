package com.mapbox.services.android.telemetry;

import android.content.Context;
import android.content.pm.PackageInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

class TelemetryUtils {
  private static final String DATE_AND_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_AND_TIME_PATTERN, Locale.US);
  public static final Locale DEFAULT_LOCALE = Locale.US;

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

  public static String getApplicationIdentifier(Context context) {
    try {
      PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      return String.format(DEFAULT_LOCALE, "%s/%s/%s", context.getPackageName(),
        packageInfo.versionName, packageInfo.versionCode);
    } catch (Exception exception) {
      return "";
    }
  }
}
