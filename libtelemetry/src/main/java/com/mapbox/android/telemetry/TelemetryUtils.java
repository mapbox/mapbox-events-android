package com.mapbox.android.telemetry;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
  private static final int UNAVAILABLE_BATTERY_LEVEL = 100;


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

  static int getVolumeLevel(Context context) {
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    return (int) Math.floor(100.0 * audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
      / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
  }

  static int getScreenBrightness(Context context) {
    int screenBrightness;
    try {
      screenBrightness = android.provider.Settings.System.getInt(
        context.getContentResolver(),
        android.provider.Settings.System.SCREEN_BRIGHTNESS);

      // Android returns values between 0 and 255, here we normalize to 0-100.
      screenBrightness = (int) Math.floor(100.0 * screenBrightness / 255.0);
    } catch (Settings.SettingNotFoundException exception) {
      screenBrightness = -1;
    }

    return screenBrightness;
  }

  static String getApplicationState(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
    if (appProcesses == null) {
      return "";
    }

    String packageName = context.getPackageName();
    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
      if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        && appProcess.processName.equals(packageName)) {
        return "Foreground";
      }
    }

    return "Background";
  }

  static int getBatteryLevel() {
    Intent batteryStatus = MapboxTelemetry.batteryStatus;

    if (batteryStatus != null) {
      int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
      int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
      return Math.round((level / (float) scale) * 100);
    } else {
      return UNAVAILABLE_BATTERY_LEVEL;
    }
  }

  static boolean isPluggedIn() {
    Intent batteryStatus = MapboxTelemetry.batteryStatus;

    if (batteryStatus != null) {
      int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
      if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB
        || chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
        return true;
      }
    }
    return false;
  }

  static String getCellularNetworkType(Context context) {
    TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    switch (manager.getNetworkType()) {
      case TelephonyManager.NETWORK_TYPE_1xRTT:
        return "1xRTT";
      case TelephonyManager.NETWORK_TYPE_CDMA:
        return "CDMA";
      case TelephonyManager.NETWORK_TYPE_EDGE:
        return "EDGE";
      case TelephonyManager.NETWORK_TYPE_EHRPD:
        return "EHRPD";
      case TelephonyManager.NETWORK_TYPE_EVDO_0:
        return "EVDO_0";
      case TelephonyManager.NETWORK_TYPE_EVDO_A:
        return "EVDO_A";
      case TelephonyManager.NETWORK_TYPE_EVDO_B:
        return "EVDO_B";
      case TelephonyManager.NETWORK_TYPE_GPRS:
        return "GPRS";
      case TelephonyManager.NETWORK_TYPE_HSDPA:
        return "HSDPA";
      case TelephonyManager.NETWORK_TYPE_HSPA:
        return "HSPA";
      case TelephonyManager.NETWORK_TYPE_HSPAP:
        return "HSPAP";
      case TelephonyManager.NETWORK_TYPE_HSUPA:
        return "HSUPA";
      case TelephonyManager.NETWORK_TYPE_IDEN:
        return "IDEN";
      case TelephonyManager.NETWORK_TYPE_LTE:
        return "LTE";
      case TelephonyManager.NETWORK_TYPE_UMTS:
        return "UMTS";
      case TelephonyManager.NETWORK_TYPE_UNKNOWN:
        return "Unknown";
      default:
        return "";
    }
  }

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
}
