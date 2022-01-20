package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class AppStateUtils {
  private static final String TAG = "AppStateUtils";

  public enum ActivityState {
    ACTIVITY_STATE_UNKNOWN(0),
    ACTIVITY_STATE_CREATED(1),
    ACTIVITY_STATE_STARTED(2),
    ACTIVITY_STATE_RESUMED(3),
    ACTIVITY_STATE_PAUSED(4),
    ACTIVITY_STATE_STOPPED(5),
    ACTIVITY_STATE_SAVE_INSTANCE_STATE(6),
    ACTIVITY_STATE_DESTROYED(7);

    private final int code;

    ActivityState(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }

    static ActivityState fromCode(int code) {
      switch (code) {
        case 0:
          return ACTIVITY_STATE_UNKNOWN;
        case 1:
          return ACTIVITY_STATE_CREATED;
        case 2:
          return ACTIVITY_STATE_STARTED;
        case 3:
          return ACTIVITY_STATE_RESUMED;
        case 4:
          return ACTIVITY_STATE_PAUSED;
        case 5:
          return ACTIVITY_STATE_STOPPED;
        case 6:
          return ACTIVITY_STATE_SAVE_INSTANCE_STATE;
        case 7:
          return ACTIVITY_STATE_DESTROYED;
        default:
          LogUtils.e(TAG, "Unknown activity status code: " + code);
          return ACTIVITY_STATE_UNKNOWN;
      }
    }
  }

  public enum AppState {
    UNKNOWN,
    FOREGROUND,
    BACKGROUND;

    @Override
    public String toString() {
      switch (this) {
        case FOREGROUND:
          return "Foreground";
        case BACKGROUND:
          return "Background";
        default:
          return "Unknown";
      }
    }
  }

  public static String PREFERENCE_FILENAME = "mb_app_state_utils";
  public static String KEY_LAST_KNOWN_ACTIVITY_STATE = "mb_telemetry_last_know_activity_state";
  private static final ScheduledThreadPoolExecutor ioExecutor =
          (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);

  public static void saveActivityState(@NonNull final Context context, @NonNull final ActivityState state) {
    ioExecutor.execute(new Runnable() {
      @SuppressLint("ApplySharedPref")
      @Override
      public void run() {
        final SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_LAST_KNOWN_ACTIVITY_STATE, state.getCode()).commit();
      }
    });
  }

  public static ActivityState getLastKnownActivityState(@NonNull Context context) {
    final SharedPreferences preferences =
        context.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
    int code = preferences.getInt(KEY_LAST_KNOWN_ACTIVITY_STATE,
        ActivityState.ACTIVITY_STATE_UNKNOWN.getCode());
    return ActivityState.fromCode(code);
  }

  private static AppState getAppStatePreLollipop(@NonNull Context context) {
    final ActivityManager activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (activityManager == null) {
      return AppState.UNKNOWN;
    }

    final String appPackageName =
        context.getApplicationContext().getPackageName();

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

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private static AppState getAppStateQAndHigher(@NonNull Context context) {
    final ActivityManager activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (activityManager == null) {
      return AppState.UNKNOWN;
    }

    AppState state = AppState.BACKGROUND;
    List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
    for (ActivityManager.AppTask task : tasks) {
      if (task.getTaskInfo().isRunning) {
        state = AppState.FOREGROUND;
      }
    }
    return state;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private static AppState getAppStateLollipopAndHigher(@NonNull Context context) {
    final ActivityManager activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (activityManager == null) {
      return AppState.UNKNOWN;
    }

    AppState state = AppState.BACKGROUND;
    List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
    for (ActivityManager.AppTask task : tasks) {
      if (task.getTaskInfo().id != -1) {
        state = AppState.FOREGROUND;
      }
    }
    return state;
  }

  static AppState getAppStateFromActivityManager(@NonNull Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return getAppStateQAndHigher(context);
    }
    if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      return getAppStateLollipopAndHigher(context);
    }
    return getAppStatePreLollipop(context);
  }

  private static boolean isActivityInactive(ActivityState activityState) {
    switch (activityState) {
      case ACTIVITY_STATE_PAUSED:
      case ACTIVITY_STATE_STOPPED:
      case ACTIVITY_STATE_SAVE_INSTANCE_STATE:
      case ACTIVITY_STATE_DESTROYED:
        return true;
      default:
        return false;
    }
  }

  private static AppState arbitrage(AppState stateFromActivityManager,
                                    ActivityState lastKnownActivityState) {
    LogUtils.v(TAG, "stateFromActivityManager = " + stateFromActivityManager
        + ", lastKnowActivityState = " + lastKnownActivityState);
    // Report background if activity is alive but not visible
    if (stateFromActivityManager == AppState.FOREGROUND
        && isActivityInactive(lastKnownActivityState)) {
      return AppState.BACKGROUND;
    }
    return stateFromActivityManager;
  }

  public interface GetAppStateCallback {
    void onReady(AppState state);
  }

  public static void getAppState(@NonNull final Context context, final GetAppStateCallback callback) {
    ioExecutor.execute(new Runnable() {
      @Override
      public void run() {
        LogUtils.v(TAG, "Getting app state...");
        final AppState state = arbitrage(getAppStateFromActivityManager(context),
                getLastKnownActivityState(context));
        LogUtils.v(TAG,"getAppState() returns " + state);
        callback.onReady(state);
      }
    });
  }
}
