package com.mapbox.android.telemetry;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import android.util.Log;

import com.mapbox.android.core.MapboxSdkInfoForUserAgentGenerator;

import okio.Buffer;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_SHARED_PREFERENCES;

public class TelemetryUtils {
  private static final String TAG = "TelemetryUtils";
  static final String MAPBOX_SHARED_PREFERENCE_KEY_VENDOR_ID = "mapboxVendorId";
  private static final String KEY_META_DATA_WAKE_UP = "com.mapbox.AdjustWakeUp";
  private static final String DATE_AND_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  private static final String EMPTY_STRING = "";
  private static final String USER_AGENT_APP_ID_FORMAT = "%s/%s (%s; %s; %s)";
  private static final String USER_AGENT_VERSION_CODE_FORMAT = "v%d";
  private static final String TWO_STRING_FORMAT = "%s %s";
  private static final String THREE_STRING_FORMAT = "%s/%s/%s";
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_AND_TIME_PATTERN, Locale.US);
  private static final Locale DEFAULT_LOCALE = Locale.US;
  private static final int UNAVAILABLE_BATTERY_LEVEL = -1;
  private static final int DEFAULT_BATTERY_LEVEL = -1;
  private static final int PERCENT_SCALE = 100;
  private static final String FOREGROUND = "Foreground";
  private static final String BACKGROUND = "Background";
  private static final String NO_STATE = "";
  private static final String SINGLE_CARRIER_RTT = "1xRTT";
  private static final String CODE_DIVISION_MULTIPLE_ACCESS = "CDMA";
  private static final String ENHANCED_DATA_GSM_EVOLUTION = "EDGE";
  private static final String ENHANCED_HIGH_RATE_PACKET_DATA = "EHRPD";
  private static final String EVOLUTION_DATA_OPTIMIZED_0 = "EVDO_0";
  private static final String EVOLUTION_DATA_OPTIMIZED_A = "EVDO_A";
  private static final String EVOLUTION_DATA_OPTIMIZED_B = "EVDO_B";
  private static final String GENERAL_PACKET_RADIO_SERVICE = "GPRS";
  private static final String HIGH_SPEED_DOWNLINK_PACKET_ACCESS = "HSDPA";
  private static final String HIGH_SPEED_PACKET_ACCESS = "HSPA";
  private static final String HIGH_SPEED_PACKET_ACCESS_PLUS = "HSPAP";
  private static final String HIGH_SPEED_UNLINK_PACKET_ACCESS = "HSUPA";
  private static final String INTEGRATED_DIGITAL_ENHANCED_NETWORK = "IDEN";
  private static final String LONG_TERM_EVOLUTION = "LTE";
  private static final String UNIVERSAL_MOBILE_TELCO_SERVICE = "UMTS";
  private static final String UNKNOWN = "Unknown";
  private static final String OPERATING_SYSTEM = "Android - " + Build.VERSION.RELEASE;
  private static final Map<Integer, String> NETWORKS = new HashMap<Integer, String>() {
    {
      put(TelephonyManager.NETWORK_TYPE_1xRTT, SINGLE_CARRIER_RTT);
      put(TelephonyManager.NETWORK_TYPE_CDMA, CODE_DIVISION_MULTIPLE_ACCESS);
      put(TelephonyManager.NETWORK_TYPE_EDGE, ENHANCED_DATA_GSM_EVOLUTION);
      put(TelephonyManager.NETWORK_TYPE_EHRPD, ENHANCED_HIGH_RATE_PACKET_DATA);
      put(TelephonyManager.NETWORK_TYPE_EVDO_0, EVOLUTION_DATA_OPTIMIZED_0);
      put(TelephonyManager.NETWORK_TYPE_EVDO_A, EVOLUTION_DATA_OPTIMIZED_A);
      put(TelephonyManager.NETWORK_TYPE_EVDO_B, EVOLUTION_DATA_OPTIMIZED_B);
      put(TelephonyManager.NETWORK_TYPE_GPRS, GENERAL_PACKET_RADIO_SERVICE);
      put(TelephonyManager.NETWORK_TYPE_HSDPA, HIGH_SPEED_DOWNLINK_PACKET_ACCESS);
      put(TelephonyManager.NETWORK_TYPE_HSPA, HIGH_SPEED_PACKET_ACCESS);
      put(TelephonyManager.NETWORK_TYPE_HSPAP, HIGH_SPEED_PACKET_ACCESS_PLUS);
      put(TelephonyManager.NETWORK_TYPE_HSUPA, HIGH_SPEED_UNLINK_PACKET_ACCESS);
      put(TelephonyManager.NETWORK_TYPE_IDEN, INTEGRATED_DIGITAL_ENHANCED_NETWORK);
      put(TelephonyManager.NETWORK_TYPE_LTE, LONG_TERM_EVOLUTION);
      put(TelephonyManager.NETWORK_TYPE_UMTS, UNIVERSAL_MOBILE_TELCO_SERVICE);
      put(TelephonyManager.NETWORK_TYPE_UNKNOWN, UNKNOWN);
    }
  };

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

  public static String obtainUniversalUniqueIdentifier() {
    String universalUniqueIdentifier = UUID.randomUUID().toString();
    return universalUniqueIdentifier;
  }

  public static String obtainApplicationState(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
    if (appProcesses == null) {
      return NO_STATE;
    }

    String packageName = context.getPackageName();
    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
      if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        && appProcess.processName.equals(packageName)) {
        return FOREGROUND;
      }
    }
    return BACKGROUND;
  }

  public static int obtainBatteryLevel(Context context) {
    Intent batteryStatus = registerBatteryUpdates(context);
    if (batteryStatus == null) {
      return UNAVAILABLE_BATTERY_LEVEL;
    }

    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, DEFAULT_BATTERY_LEVEL);
    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, DEFAULT_BATTERY_LEVEL);
    if (level < 0 || scale <= 0) {
      return UNAVAILABLE_BATTERY_LEVEL;
    }
    return Math.round((level / (float) scale) * PERCENT_SCALE);
  }

  public static boolean isPluggedIn(Context context) {
    Intent batteryStatus = registerBatteryUpdates(context);
    if (batteryStatus == null) {
      return false;
    }

    int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, DEFAULT_BATTERY_LEVEL);
    final boolean pluggedIntoUSB = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
    final boolean pluggedIntoAC = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
    return pluggedIntoUSB || pluggedIntoAC;
  }

  public static String obtainCellularNetworkType(Context context) {
    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    return NETWORKS.get(telephonyManager.getNetworkType());
  }

  public static String obtainCurrentDate() {
    return dateFormat.format(new Date());
  }

  public static String generateCreateDateFormatted(Date date) {
    return dateFormat.format(date);
  }

  static String createFullUserAgent(String userAgent, Context context) {
    String appIdentifier = TelemetryUtils.obtainApplicationIdentifier(context);
    String newUserAgent = toHumanReadableAscii(String.format(DEFAULT_LOCALE, TWO_STRING_FORMAT, appIdentifier,
      userAgent));
    String fullUserAgent = TextUtils.isEmpty(appIdentifier) ? userAgent : newUserAgent;

    return fullUserAgent;
  }

  static String createReformedFullUserAgent(Context context) {
    String appIdentifier = TelemetryUtils.obtainApplicationIdentifierForReformedUserAgent(context);
    String reformedUserAgent = toHumanReadableAscii(String.format(DEFAULT_LOCALE, TWO_STRING_FORMAT, appIdentifier,
      MapboxSdkInfoForUserAgentGenerator.getInstance(context.getAssets()).getSdkInfoForUserAgent()));
    return TextUtils.isEmpty(reformedUserAgent) ? appIdentifier : reformedUserAgent;
  }

  static boolean isEmpty(String string) {
    if (string == null || string.length() == 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Do not call this method outside of activity!!!
   */
  public static String retrieveVendorId() {
    if (MapboxTelemetry.applicationContext == null) {
      return updateVendorId();
    }

    SharedPreferences sharedPreferences = obtainSharedPreferences(MapboxTelemetry.applicationContext);
    String mapboxVendorId = sharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_VENDOR_ID, "");
    if (TelemetryUtils.isEmpty(mapboxVendorId)) {
      mapboxVendorId = TelemetryUtils.updateVendorId();
    }
    return mapboxVendorId;
  }

  static SharedPreferences obtainSharedPreferences(Context context) {
    return context.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE);
  }

  private static String updateVendorId() {
    String uniqueId = obtainUniversalUniqueIdentifier();
    if (MapboxTelemetry.applicationContext == null) {
      return uniqueId;
    }

    SharedPreferences sharedPreferences = obtainSharedPreferences(MapboxTelemetry.applicationContext);
    SharedPreferences.Editor editor = sharedPreferences.edit();
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

  private static String obtainApplicationIdentifierForReformedUserAgent(Context context) {
    try {
      String packageName = context.getPackageName();
      PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
      String versionCode = String.format(DEFAULT_LOCALE, USER_AGENT_VERSION_CODE_FORMAT, packageInfo.versionCode);
      String version = (packageInfo.versionName == null) ? "0" : packageInfo.versionName;
      String appIdentifier = String.format(DEFAULT_LOCALE, USER_AGENT_APP_ID_FORMAT,
        getApplicationName(context), version, packageName, versionCode, OPERATING_SYSTEM);
      return appIdentifier;
    } catch (Exception exception) {
      return EMPTY_STRING;
    }
  }

  private static String getApplicationName(Context context) {
    final PackageManager packagemanager = context.getApplicationContext().getPackageManager();
    ApplicationInfo applicationInfo;
    try {
      applicationInfo = packagemanager.getApplicationInfo(context.getPackageName(), 0);
    } catch (final PackageManager.NameNotFoundException nameNotFoundException) {
      applicationInfo = null;
    }
    String unknown = "(unknown)";
    return (String) (applicationInfo != null ? packagemanager.getApplicationLabel(applicationInfo) : unknown);
  }

  /**
   * This method can return null on devices without
   * any battery (like a TV) or because of buggy firmware.
   *
   * @param context application context
   * @return intent
   */
  @Nullable
  private static Intent registerBatteryUpdates(Context context) {
    try {
      return context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    } catch (Exception exc) {
      // There is a weird intermittent bug that causes SecurityException in android versions <= 4.2
      Log.e(TAG, String.format("%s: Failed receiver registration for ACTION_BATTERY_CHANGED", exc.toString()));
      return null;
    }
  }

  static boolean adjustWakeUpMode(Context context) {
    try {
      ApplicationInfo appInformation = context.getPackageManager().getApplicationInfo(context.getPackageName(),
        PackageManager.GET_META_DATA);
      if (appInformation != null && appInformation.metaData != null) {
        boolean adjustWakeUp = appInformation.metaData.getBoolean(KEY_META_DATA_WAKE_UP, false);
        return adjustWakeUp;
      }
    } catch (PackageManager.NameNotFoundException exception) {
      exception.printStackTrace();
    }
    return false;
  }
}
