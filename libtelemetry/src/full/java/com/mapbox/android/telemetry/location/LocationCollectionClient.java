package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.mapbox.android.core.location.LocationEngineProvider;

import java.util.concurrent.atomic.AtomicBoolean;

public class LocationCollectionClient {
  private static final int LOCATION_COLLECTION_STATUS_UPDATED = 0;
  private static final int SESSION_ROTATION_INTERVAL_UPDATED = 1;
  private static final Object lock = new Object();
  private static LocationCollectionClient locationCollectionClient;

  private final AtomicBoolean isEnabled = new AtomicBoolean(false);
  private final LocationEngineController locationEngineController;
  private final HandlerThread settingsChangeHandlerThread;
  private final Handler settingsChangeHandler;

  @VisibleForTesting
  LocationCollectionClient(LocationEngineController collectionController,
                           HandlerThread handlerThread) {
    this.locationEngineController = collectionController;
    this.settingsChangeHandlerThread = handlerThread;
    this.settingsChangeHandlerThread.start();
    this.settingsChangeHandler = new Handler(handlerThread.getLooper()) {
      @Override
      public void handleMessage(Message msg) {
        handleSettingsChangeMessage(msg);
      }
    };
  }

  public static LocationCollectionClient install(@NonNull Context context,
                                                 @NonNull SessionIdentifier sessionIdentifier) {
    Context applicationContext;
    if (context.getApplicationContext() == null) {
      // In shared processes content providers getApplicationContext() can return null.
      applicationContext = context;
    } else {
      applicationContext = context.getApplicationContext();
    }

    synchronized (lock) {
      if (locationCollectionClient == null) {
        locationCollectionClient = new LocationCollectionClient(new LocationEngineControllerImpl(applicationContext,
          LocationEngineProvider.getBestLocationEngine(applicationContext), sessionIdentifier),
          new HandlerThread("LocationSettingsChangeThread"));
      }
    }
    return locationCollectionClient;
  }

  @NonNull
  public static LocationCollectionClient getInstance() {
    synchronized (lock) {
      if (locationCollectionClient != null) {
        return locationCollectionClient;
      } else {
        throw new IllegalStateException("LocationCollectionClient is not installed.");
      }
    }
  }

  public void setSessionRotationInterval(long interval) {
    Message message = Message.obtain();
    message.what = SESSION_ROTATION_INTERVAL_UPDATED;
    Bundle b = new Bundle();
    b.putLong("interval", interval);
    message.setData(b);
    settingsChangeHandler.sendMessage(message);
  }

  public boolean isEnabled() {
    return isEnabled.get();
  }

  public void setEnabled(boolean enabled) {
    if (isEnabled.compareAndSet(!enabled, enabled)) {
      settingsChangeHandler.sendEmptyMessage(LOCATION_COLLECTION_STATUS_UPDATED);
    }
  }

  private void handleSettingsChangeMessage(Message msg) {
    switch (msg.what) {
      case LOCATION_COLLECTION_STATUS_UPDATED:
        if (isEnabled()) {
          locationEngineController.onResume();
        } else {
          locationEngineController.onDestroy();
        }
        break;
      case SESSION_ROTATION_INTERVAL_UPDATED:
        Bundle bundle = msg.getData();
        long interval = bundle.getLong("interval");
        locationEngineController.setSessionIdentifier(new SessionIdentifier(interval));
        break;
      default:
        break;
    }
  }
}