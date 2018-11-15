package com.mapbox.android.telemetry;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.RequestBody;

class MetricUtils {
  private Date utcDate;
  private static int timeDrift;
  private static String configResponse;
  private static int appWakeUps = 0;
  private static Location latestLocation;
  private int requests;
  private int totalDataTransfer;
  private int cellDataTransfer;
  private int wifiDataTransfer;
  private int eventCountFailed;
  private int eventCountTotal;
  private int eventCountMax;
  private Map<String, Integer> eventCountPerType;
  private Map<String, Integer> failedRequests;
  private int potentialFailures;

  MetricUtils() {
    resetCounters();
  }

  static void setConfigResponse(String response) {
    configResponse = response;
  }

  static void setLatestLocation(Location location) {
    latestLocation = location;
  }

  int getAppWakeups() {
    return appWakeUps;
  }

  int getTimeDrift() {
    return timeDrift;
  }

  String getConfigResponse() {
    return configResponse;
  }

  Location getLatestLocation() {
    return latestLocation;
  }

  Map<String, Integer> calculateEventCountByType(List<Event> eventsQueue,
                                                 @Nullable Map<String, Integer> eventCountByType) {
    if (eventsQueue.size() > 0) {
      if (eventCountByType == null) {
        eventCountByType = new HashMap<>();
      }

      for (Event event: eventsQueue) {
        String key = String.valueOf(event.obtainType());
        if (eventCountByType.containsKey(key)) {
          eventCountByType.put(key, eventCountByType.get(key) + 1);
        } else {
          eventCountByType.put(key, 1);
        }
      }
    }

    return eventCountByType;
  }

  boolean connectedToWifi(Context context) {
    NetworkInfo networkInfo = activeNetwork(context);

    if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
      return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    } else {
      return false;
    }
  }

  boolean isNewDate() {
    if (utcDate == null) {
      utcDate = new Date();
      return false;
    }

    Calendar calendarPluseOneDay = Calendar.getInstance();
    calendarPluseOneDay.setTime(utcDate);
    calendarPluseOneDay.add(Calendar.DAY_OF_MONTH, 1);

    return System.currentTimeMillis() > calendarPluseOneDay.getTimeInMillis();
  }

  String getDateString() {
    SimpleDateFormat format = new SimpleDateFormat("YYYY-MMM-DD");
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    return format.format(utcDate);
  }

  MetricEvent buildMetricEvent() {
    MetricEvent metricEvent = new MetricEvent();
    metricEvent.setDateUTC(getDateString());
    metricEvent.setRequests(requests);
    metricEvent.setFailedRequests(convertMapToJson(failedRequests));
    metricEvent.setTotalDataTransfer(totalDataTransfer);
    metricEvent.setCellDataTransfer(cellDataTransfer);
    metricEvent.setWifiDataTransfer(wifiDataTransfer);
    metricEvent.setAppWakeups(appWakeUps);
    metricEvent.setEventCountPerType(convertMapToJson(eventCountPerType));
    metricEvent.setEventCountFailed(eventCountFailed);
    metricEvent.setEventCountTotal(eventCountTotal);
    metricEvent.setEventCountMax(eventCountMax);
    metricEvent.setDeviceTimeDrift(getTimeDrift());
    metricEvent.setConfigResponse(getConfigResponse());

    Location latestLocation = getLatestLocation();
    if (latestLocation != null) {
      metricEvent.setDeviceLat(latestLocation.getLatitude());
      metricEvent.setDeviceLon(latestLocation.getLongitude());
    }

    return metricEvent;
  }

  void resetCounters() {
    requests = 0;
    totalDataTransfer = 0;
    cellDataTransfer = 0;
    wifiDataTransfer = 0;
    appWakeUps = 0;
    eventCountFailed = 0;
    eventCountTotal = 0;
    eventCountMax = 0;
  }

  void grabSentBytes(RequestBody requestBody) {
    try {
      int sentBytes = (int) requestBody.contentLength();
      totalDataTransfer = totalDataTransfer + sentBytes;

      if (connectedToWifi(MapboxTelemetry.applicationContext)) {
        wifiDataTransfer = wifiDataTransfer + sentBytes;
      } else {
        cellDataTransfer = cellDataTransfer + sentBytes;
      }
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  void incrementRequests() {
    requests++;
  }

  void updateEventCountTotal(int eventsSize) {
    eventCountTotal = eventCountTotal + eventsSize;
  }

  void setPotentialFailures(int potentialFailures) {
    this.potentialFailures = potentialFailures;
  }

  void updateEventCountyPerType(List<Event> events) {
    eventCountPerType = calculateEventCountByType(events, eventCountPerType);
  }

  void updateFailedRequests(int code) {
    failedRequests = calculateFailedRequests(code, failedRequests);
  }

  void updateEventCountFailed() {
    eventCountFailed = eventCountFailed + potentialFailures;
  }

  static void incrementAppWakeups() {
    appWakeUps++;
  }

  static void calculateTimeDiff(long serverTime) {
    timeDrift = (int) (serverTime - System.currentTimeMillis());
  }

  Map<String, Integer> calculateFailedRequests(int code, @Nullable Map<String, Integer> failedRequests) {
    if (failedRequests == null) {
      failedRequests = new HashMap<String, Integer>();
    }

    String key = String.valueOf(code);

    if (failedRequests.containsKey(key)) {
      failedRequests.put(key, failedRequests.get(key) + 1);
    } else {
      failedRequests.put(key, 1);
    }

    return failedRequests;
  }

  String convertMapToJson(@NonNull Map<String, Integer> eventCountByType) {
    Gson gson = new Gson();
    return gson.toJson(eventCountByType);
  }

  private NetworkInfo activeNetwork(Context context) {
    ConnectivityManager connectivityManager =
      (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return connectivityManager.getActiveNetworkInfo();
  }
}
