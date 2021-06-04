package com.mapbox.android.telemetry;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.mapbox.android.telemetry.AppStateUtils.ActivityState;

public class MapboxTelemetryService extends Service {
  private final String TAG = "MapboxTelemetryService";
  private final IBinder binder = new Binder();
  private Application application = null;

  public class Binder extends android.os.Binder {
    public MapboxTelemetryService getService() {
      return MapboxTelemetryService.this;
    }
  }

  private final Application.ActivityLifecycleCallbacks
      activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity,
                                    @Nullable Bundle savedInstanceState) {
          saveActivityState(ActivityState.ACTIVITY_STATE_CREATED);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
          saveActivityState(ActivityState.ACTIVITY_STATE_STARTED);
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
          saveActivityState(ActivityState.ACTIVITY_STATE_RESUMED);
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
          saveActivityState(ActivityState.ACTIVITY_STATE_PAUSED);
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
          saveActivityState(ActivityState.ACTIVITY_STATE_STOPPED);
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity,
                                               @NonNull Bundle outState) {
          saveActivityState(ActivityState.ACTIVITY_STATE_SAVE_INSTANCE_STATE);
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
          saveActivityState(ActivityState.ACTIVITY_STATE_DESTROYED);
        }
      };

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  private void saveActivityState(ActivityState state) {
    LogUtils.v(TAG, "Activity state: " + state);
    AppStateUtils.saveActivityState(this, state);
  }

  private void resetActivityStateToUnknown() {
    AppStateUtils.saveActivityState(this, ActivityState.ACTIVITY_STATE_UNKNOWN);
  }

  @Override
  public void onCreate() {
    LogUtils.d(TAG, "Starting telemetry service...");
    resetActivityStateToUnknown();
    application = getApplication();
    application
        .registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
  }

  @Override
  public void onDestroy() {
    LogUtils.d(TAG, "Stopping telemetry service..");
    application
        .unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
  }
}
