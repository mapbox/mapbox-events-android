package com.mapbox.android.core.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import static com.mapbox.android.core.location.Utils.isBetterLocation;

/**
 * A base wrap class for LocationListener, contains the logic of downsample
 * location update frequent.
 */
public class LocationCallbackTransport implements LocationListener {
  private static final String TAG = "CallbackTransport";
  protected final LocationEngineCallback<LocationEngineResult> callback;
  private Location currentBestLocation;
  private long lastUpdateTime = 0;
  private long fastestInterval = 0;

  LocationCallbackTransport(LocationEngineCallback<LocationEngineResult> callback) {
    this.callback = callback;
  }

  @Override
  public void onLocationChanged(Location location) {
    if (isBetterLocation(location, currentBestLocation)) {
      currentBestLocation = location;
    }
    long currentTime = System.currentTimeMillis();
    long interval = currentTime - lastUpdateTime;
    if (interval < fastestInterval) {
      return;
    }
    lastUpdateTime = currentTime;

    if (callback != null) {
      callback.onSuccess(LocationEngineResult.create(currentBestLocation));
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
}
