package com.mapbox.android.core.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import static com.mapbox.android.core.location.Utils.isBetterLocation;

/**
 * A base wrap class for LocationListener, contains the logic of downsample
 * location update frequent.
 */
public class LocationCallbackTransport implements LocationListener {
  private static final String TAG = "CallbackTransport";
  private static final int MESSAGE_UPDATE_LOCATION = 0;
  private static final int MESSAGE_NOT_CACHE_PERIOD = 1;
  protected final LocationEngineCallback<LocationEngineResult> callback;
  private Location currentBestLocation;
  private long fastestInterval = 0;
  private Location cacheLocation;
  private Handler handler;

  LocationCallbackTransport(LocationEngineCallback<LocationEngineResult> callback) {
    this.callback = callback;
    handler = new ThresholdHandler();
  }

  class ThresholdHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MESSAGE_UPDATE_LOCATION:
          Location location = cacheLocation;
          cacheLocation = null;
          sendLocation(location);
          break;
        default:
          break;
      }
    }
  }

  @Override
  public void onLocationChanged(Location location) {
    if (fastestInterval != 0) {
      if (!handler.hasMessages(MESSAGE_UPDATE_LOCATION)) {
        //haven't send message in the previous fastInterval period, so send it directly.
        sendLocation(location);
      } else {
        if (!handler.hasMessages(MESSAGE_NOT_CACHE_PERIOD)) {
          cacheLocation = location;
        }
      }
    } else {
      sendLocation(location);
    }
  }

  private void sendLocation(Location location) {
    if (location != null && callback != null) {
      if (isBetterLocation(location, currentBestLocation)) {
        currentBestLocation = location;
      }
      callback.onSuccess(LocationEngineResult.create(currentBestLocation));
      if (fastestInterval != 0) {
        handler.sendEmptyMessageDelayed(MESSAGE_UPDATE_LOCATION, fastestInterval);
        //On the previous fastestInterval * 0.85 period, we will not cache location since it might not be precious.
        handler.sendEmptyMessageDelayed(MESSAGE_NOT_CACHE_PERIOD, (long) (fastestInterval * 0.85));
      }
    }
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
    handler.removeMessages(MESSAGE_UPDATE_LOCATION);
  }
}
