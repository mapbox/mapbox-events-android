package com.mapbox.android.telemetry;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

class MetricUtils {
  private Date utcDate;
  private static long timeDrift;
  private static String configResponse;
  private int appWakeups;

  void setAppWakeups(int appWakeups) {
    this.appWakeups = appWakeups;
  }

  static void setConfigResponse(String response) {
    configResponse = response;
  }

  Date getUtcDate() {
    return utcDate;
  }

  int getAppWakeups() {
    return appWakeups;
  }

  long getTimeDrift() {
    return timeDrift;
  }

  Map<String, Integer> calculateEventCountByType(List<Event> eventsQueue,
                                                 @Nullable Map<String, Integer> eventCountByType) {
    if (eventsQueue.size() > 0) {
      if (eventCountByType == null) {
        eventCountByType = new HashMap<String, Integer>();
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

    return System.currentTimeMillis() > utcDate.getTime();
  }

  String getDateString() {
    SimpleDateFormat format = new SimpleDateFormat("YYYY-MMM-DD");
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    return format.format(utcDate);
  }

  static void calculateTimeDiff(long serverTime) {
    timeDrift = serverTime - System.currentTimeMillis();
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
