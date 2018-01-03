package com.mapbox.services.android.telemetry;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;

public class MapEventFactory {
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
  private static final String LANDSCAPE = "Landscape";
  private static final String PORTRAIT = "Portrait";
  private static final String NO_CARRIER = "EMPTY_CARRIER";
  private static final int UNAVAILABLE_BATTERY_LEVEL = -1;
  private static final int DEFAULT_BATTERY_LEVEL = -1;
  private static final int NO_NETWORK = -1;
  private static final String NOT_A_LOAD_MAP_EVENT_TYPE = "Type must be a load map event.";
  private static final String NOT_A_GESTURE_MAP_EVENT_TYPE = "Type must be a gesture map event.";
  private static final String MAP_STATE_ILLEGAL_NULL = "MapState cannot be null.";
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
  private static final Map<Integer, String> ORIENTATIONS = new HashMap<Integer, String>() {
    {
      put(Configuration.ORIENTATION_LANDSCAPE, LANDSCAPE);
      put(Configuration.ORIENTATION_PORTRAIT, PORTRAIT);
    }
  };
  private Context context = null;
  private final Map<Event.Type, MapBuildEvent> BUILD_EVENT_MAP_GESTURE = new HashMap<Event.Type, MapBuildEvent>() {
    {
      put(Event.Type.MAP_CLICK, new MapBuildEvent() {
        @Override
        public Event build(Context context, MapState mapState) {
          return buildMapClickEvent(context, mapState);
        }
      });
      put(Event.Type.MAP_DRAGEND, new MapBuildEvent() {
        @Override
        public Event build(Context context, MapState mapState) {
          return buildMapDragendEvent(context, mapState);
        }
      });
    }
  };

  public MapEventFactory(Context context) {
    this.context = context;
  }

  public Event createMapLoadEvent(Event.Type type) {
    checkLoad(type);
    return buildMapLoadEvent(context);
  }

  public Event createMapGestureEvent(Event.Type type, MapState mapState) {
    checkGesture(type, mapState);
    return BUILD_EVENT_MAP_GESTURE.get(type).build(context, mapState);
  }

  private MapClickEvent buildMapClickEvent(Context context, MapState mapState) {
    MapClickEvent mapClickEvent = new MapClickEvent(mapState);

    mapClickEvent.setOrientation(obtainOrientation(context));
    mapClickEvent.setCarrier(obtainCellularCarrier(context));
    mapClickEvent.setCellularNetworkType(obtainCellularNetworkType(context));
    mapClickEvent.setBatteryLevel(obtainBatteryLevel());
    mapClickEvent.setPluggedIn(isPluggedIn());
    mapClickEvent.setWifi(obtainConnectedToWifi(context));

    return mapClickEvent;
  }

  private MapDragendEvent buildMapDragendEvent(Context context, MapState mapState) {
    MapDragendEvent mapDragendEvent = new MapDragendEvent(mapState);

    mapDragendEvent.setOrientation(obtainOrientation(context));
    mapDragendEvent.setCarrier(obtainCellularCarrier(context));
    mapDragendEvent.setCellularNetworkType(obtainCellularNetworkType(context));
    mapDragendEvent.setBatteryLevel(obtainBatteryLevel());
    mapDragendEvent.setPluggedIn(isPluggedIn());
    mapDragendEvent.setWifi(obtainConnectedToWifi(context));

    return mapDragendEvent;
  }

  private String obtainOrientation(Context context) {
    return ORIENTATIONS.get(context.getResources().getConfiguration().orientation);
  }

  private float obtainAccessibilityFontScaleSize(Context context) {
    return context.getResources().getConfiguration().fontScale;
  }

  private String obtainCellularCarrier(Context context) {
    TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    String carrierName = manager.getNetworkOperatorName();

    if (TextUtils.isEmpty(carrierName)) {
      return NO_CARRIER;
    }

    return carrierName;
  }

  private String obtainCellularNetworkType(Context context) {
    TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    return NETWORKS.get(manager.getNetworkType());
  }

  private float obtainDisplayDensity(Context context) {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

    return displayMetrics.density;
  }

  private int obtainBatteryLevel() {
    Intent batteryStatus = registerBatteryUpdates(context);

    if (batteryStatus == null) {
      return UNAVAILABLE_BATTERY_LEVEL;
    }

    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, DEFAULT_BATTERY_LEVEL);
    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, DEFAULT_BATTERY_LEVEL);
    return Math.round((level / (float) scale) * 100);
  }

  private Intent registerBatteryUpdates(Context context) {
    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    return context.registerReceiver(null, filter);
  }

  private boolean isPluggedIn() {
    Intent batteryStatus = registerBatteryUpdates(context);
    int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, DEFAULT_BATTERY_LEVEL);
    final boolean pluggedIntoUSB = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
    final boolean pluggedIntoAC = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

    return pluggedIntoUSB || pluggedIntoAC;
  }

  private Boolean obtainConnectedToWifi(Context context) {
    return isConnectedToWifi(context);
  }

  private boolean isConnectedToWifi(Context context) {
    try {
      WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
      //noinspection MissingPermission
      WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

      return isWifiConnected(wifiMgr, wifiInfo);
    } catch (Exception exception) {
      return false;
    }
  }

  private boolean isWifiConnected(WifiManager wifiMgr, WifiInfo wifiInfo) {
    return wifiMgr.isWifiEnabled() && networkConnected(wifiInfo);
  }

  private boolean networkConnected(WifiInfo wifiInfo) {
    if (wifiInfo.getNetworkId() != NO_NETWORK) {
      return true;
    }
    return false;
  }

  private MapLoadEvent buildMapLoadEvent(Context context) {
    MapLoadEvent mapLoadEvent = new MapLoadEvent();

    mapLoadEvent.setUserId(TelemetryUtils.obtainUniversalUniqueIdentifier());
    mapLoadEvent.setOrientation(obtainOrientation(context));
    mapLoadEvent.setAccessibilityFontScale(obtainAccessibilityFontScaleSize(context));
    mapLoadEvent.setCarrier(obtainCellularCarrier(context));
    mapLoadEvent.setCellularNetworkType(obtainCellularNetworkType(context));
    mapLoadEvent.setResolution(obtainDisplayDensity(context));
    mapLoadEvent.setBatteryLevel(obtainBatteryLevel());
    mapLoadEvent.setPluggedIn(isPluggedIn());
    mapLoadEvent.setWifi(obtainConnectedToWifi(context));

    return mapLoadEvent;
  }

  private void checkLoad(Event.Type type) {
    if (type != Event.Type.MAP_LOAD) {
      throw new IllegalArgumentException(NOT_A_LOAD_MAP_EVENT_TYPE);
    }
  }

  private void checkGesture(Event.Type type, MapState mapState) {
    checkGestureMapEvent(type);
    isNotNull(mapState);
  }

  private void checkGestureMapEvent(Event.Type type) {
    if (!Event.mapGestureEventTypes.contains(type)) {
      throw new IllegalArgumentException(NOT_A_GESTURE_MAP_EVENT_TYPE);
    }
  }

  private void isNotNull(MapState mapState) {
    if (mapState == null) {
      throw new IllegalArgumentException(MAP_STATE_ILLEGAL_NULL);
    }
  }
}
