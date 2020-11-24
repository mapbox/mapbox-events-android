package com.mapbox.android.telemetry.location;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.telemetry.MapboxTelemetry;

import java.util.List;

/**
 * Broadcast receiver through which location updates are reported.
 * This receiver is optimized for the background location updates use case.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = "LocationUpdateReceiver";
  static final String ACTION_LOCATION_UPDATED =
    "com.mapbox.android.telemetry.location.locationupdatespendingintent.action.LOCATION_UPDATED";
  private ActivityManager activityManager;
  private String appPackageName;

  enum AppState {
    UNKNOWN,
    FOREGROUND,
    BACKGROUND;

    @Override
    public String toString() {
      switch (this) {
        case FOREGROUND: return  "Foreground";
        case BACKGROUND: return  "Background";
        default:         return  "Unknown";
      }
    }
  }

  private AppState getAppStatePreLollipop() {
    AppState state = AppState.UNKNOWN;
    final int maxNumTasksToAsk = 32;

    List<ActivityManager.RunningTaskInfo> tasks =
            activityManager.getRunningTasks(maxNumTasksToAsk);

    for (ActivityManager.RunningTaskInfo task : tasks) {
      if (task.topActivity != null) {
        if (task.topActivity.getPackageName().equals(appPackageName)) {
          state = AppState.FOREGROUND;
        }
      }
    }

    if ((tasks.size() < maxNumTasksToAsk) && state == AppState.UNKNOWN) {
      state = AppState.BACKGROUND;
    }

    return state;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private AppState getAppStateLollipopAndHigher() {
    AppState state = AppState.BACKGROUND;
    List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
    for (ActivityManager.AppTask task : tasks) {
      if (task.getTaskInfo().id != -1) {
        state = AppState.FOREGROUND;
      }
    }
    return state;
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private AppState getAppStateQAndHigher() {
    AppState state = AppState.BACKGROUND;
    List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
    for (ActivityManager.AppTask task : tasks) {
      if (task.getTaskInfo().isRunning) {
        state = AppState.FOREGROUND;
      }
    }
    return state;
  }

  private AppState getAppState() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return getAppStateQAndHigher();
    }
    if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      return getAppStateLollipopAndHigher();
    }
    return getAppStatePreLollipop();
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      if (intent == null) {
        Log.w(TAG, "intent == null");
        return;
      }

      activityManager =
              (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
      appPackageName = context.getApplicationContext().getPackageName();

      final String action = intent.getAction();
      if (!ACTION_LOCATION_UPDATED.equals(action)) {
        return;
      }

      LocationEngineResult result = LocationEngineResult.extractResult(intent);
      if (result == null) {
        Log.w(TAG, "LocationEngineResult == null");
        return;
      }

      LocationCollectionClient collectionClient =  LocationCollectionClient.getInstance();
      MapboxTelemetry telemetry = collectionClient.getTelemetry();
      String sessionId = collectionClient.getSessionId();
      List<Location> locations = result.getLocations();
      for (Location location : locations) {
        if (isThereAnyNaN(location) || isThereAnyInfinite(location)) {
          continue;
        }
        final String appState = getAppState().toString();
        telemetry.push(LocationMapper.create(location, appState, sessionId));
      }
    } catch (Throwable throwable) {
      // TODO: log silent crash
      Log.e(TAG, throwable.toString());
    }
  }

  private static boolean isThereAnyNaN(Location location) {
    return Double.isNaN(location.getLatitude()) || Double.isNaN(location.getLongitude())
      || Double.isNaN(location.getAltitude()) || Float.isNaN(location.getAccuracy());
  }

  private static boolean isThereAnyInfinite(Location location) {
    return Double.isInfinite(location.getLatitude()) || Double.isInfinite(location.getLongitude())
      || Double.isInfinite(location.getAltitude()) || Float.isInfinite(location.getAccuracy());
  }
}
