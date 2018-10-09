package com.mapbox.android.telemetry;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;

class MetricUtils {
  private Date utcDate;
  private int timeDrift;
  private int appWakeups;

  void setAppWakeups(int appWakeups) {
    this.appWakeups = appWakeups;
  }

  void setTimeDrift(int timeDrift) {
    this.timeDrift = timeDrift;
  }

  public Date getUtcDate() {
    return utcDate;
  }

  int getAppWakeups() {
    return appWakeups;
  }

  public int getTimeDrift() {
    return timeDrift;
  }

  Map<String, Integer> calculateEventCountByType(EventsQueue eventsQueue,
                                                 @Nullable Map<String, Integer> eventCountByType) {
    if (eventsQueue.queue.size() > 0) {
      if (eventCountByType == null) {
        eventCountByType = new HashMap<String, Integer>();
      }

      Queue<Event> queue = eventsQueue.queue.obtainQueue();

      for (Event event: queue) {
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
    return format.format(new Date());
  }

  Map<String, Integer> calculateFailedRequests(EventsQueue eventsQueue,
                                                 @Nullable Map<String, Integer> eventCountByType) {

    return eventCountByType;
  }

  private NetworkInfo activeNetwork(Context context) {
    ConnectivityManager connectivityManager =
      (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return connectivityManager.getActiveNetworkInfo();
  }
}
