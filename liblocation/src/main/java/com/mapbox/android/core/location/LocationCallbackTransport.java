package com.mapbox.android.core.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import static com.mapbox.android.core.location.Utils.isBetterLocation;

/**
 * A base wrap class for LocationListener, contains the logic of downsample
 * location update frequent.
 */
public class LocationCallbackTransport implements LocationListener {
  private static final String TAG = "CallbackTransport";
  private static final int MESSAGE_WHAT = 0;
  protected final LocationEngineCallback<LocationEngineResult> callback;
  private Location currentBestLocation;
  private long fastestInterval = 0;
  private Location cacheLocation;
  private Handler handler;
  private HandlerThread handlerThread;

  LocationCallbackTransport(LocationEngineCallback<LocationEngineResult> callback) {
    this.callback = callback;
    handlerThread = new HandlerThread(this.getClass().getName());
    handlerThread.start();
    handler = new ThresholdHandler(handlerThread.getLooper());
  }

  class ThresholdHandler extends Handler {
    ThresholdHandler(Looper looper) {
      super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
      Location location = cacheLocation;
      cacheLocation = null;
      if (location != null && sendLocation(location)) {
        //send location success, start to send next one after fastestInterval period
        this.sendEmptyMessageDelayed(MESSAGE_WHAT, fastestInterval);
      }
    }
  }

  @Override
  public void onLocationChanged(Location location) {
    if (fastestInterval != 0) {
      if (!handler.hasMessages(MESSAGE_WHAT)) {
        //haven't send message in the previous fastInterval period, so send it directly.
        sendLocation(location);
        handler.sendEmptyMessageDelayed(MESSAGE_WHAT, fastestInterval);
      } else {
        //cacheLocation will always be the latest one.
        cacheLocation = location;
      }
    } else {
      sendLocation(location);
    }
  }

  private boolean sendLocation(Location location) {
    if (callback != null) {
      if (isBetterLocation(location, currentBestLocation)) {
        currentBestLocation = location;
      }
      callback.onSuccess(LocationEngineResult.create(currentBestLocation));
      return true;
    }
    return false;
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    Log.d(TAG, "onStatusChanged: " + provider);
  }

  @Override
  public void onProviderEnabled(String provider) {
    Log.d(TAG, "onProviderEnabled: " + provider);
  }

  @Override
  public void onProviderDisabled(String provider) {
    Log.d(TAG, "onProviderDisabled: " + provider);
  }

  /**
   * Set the fastestInterval between two adjacent updates.
   *
   * @param fastestInterval in millisecond.
   */
  void setFastInterval(long fastestInterval) {
    this.fastestInterval = fastestInterval;
  }

  void onDestroy() {
    handler.removeMessages(MESSAGE_WHAT);
    handlerThread.quit();
  }
}
