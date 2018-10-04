package com.mapbox.android.core.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * A location engine that uses core android.location and has no external dependencies
 * https://developer.android.com/guide/topics/location/strategies.html
 */
class AndroidLocationEngine extends AbstractLocationEngine<LocationListener> implements LocationEngine {
  private static final String TAG = "AndroidLocationEngine";
  private final LocationManager locationManager;

  private String currentProvider = LocationManager.PASSIVE_PROVIDER;

  AndroidLocationEngine(@NonNull Context context) {
    super();
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
  }

  @NonNull
  @Override
  protected LocationListener getListener(final LocationEngineCallback<Location> callback) {
    return new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        callback.onSuccess(location);
      }

      @Override
      public void onStatusChanged(String s, int i, Bundle bundle) {
        // noop
      }

      @Override
      public void onProviderEnabled(String s) {
        // noop
      }

      @Override
      public void onProviderDisabled(String s) {
        callback.onFailure(new Exception("Current provider disabled"));
      }
    };
  }

  @Override
  protected void removeListener(@NonNull LocationListener listener) {
    locationManager.removeUpdates(listener);
  }

  @Override
  public void getLastLocation(@NonNull LocationEngineCallback<Location> callback) throws SecurityException {
    Location lastLocation = null;
    try {
      lastLocation = locationManager.getLastKnownLocation(currentProvider);
    } catch (IllegalArgumentException iae) {
      Log.e(TAG, iae.toString());
    }

    if (lastLocation != null) {
      callback.onSuccess(lastLocation);
    } else {
      callback.onFailure(new Exception("Last location unavailable"));
    }
  }

  @Override
  public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                     @NonNull LocationEngineCallback<Location> callback,
                                     @Nullable Looper looper) throws SecurityException {
    LocationListener locationListener = mapLocationListener(callback);

    // Pick best provider only if user has not explicitly chosen passive mode
    if (request.getPriority() != LocationEngineRequest.PRIORITY_NO_POWER) {
      currentProvider = locationManager.getBestProvider(getCriteria(request.getPriority()), true);
    }

    locationManager.requestLocationUpdates(currentProvider, request.getInterval(), request.getDisplacemnt(),
            locationListener, looper);
  }

  @Override
  public void removeLocationUpdates(@NonNull LocationEngineCallback<Location> callback) {
    LocationListener listener = unmapLocationListener(callback);
    if (listener != null) {
      locationManager.removeUpdates(listener);
    }
  }

  private static Criteria getCriteria(int priority) {
    Criteria criteria = new Criteria();
    criteria.setAccuracy(priorityToAccuracy(priority));
    criteria.setCostAllowed(true);
    criteria.setPowerRequirement(priorityToPowerRequirement(priority));
    return criteria;
  }

  private static int priorityToAccuracy(int priority) {
    switch (priority) {
      case LocationEngineRequest.PRIORITY_HIGH_ACCURACY:
      case LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY:
        return Criteria.ACCURACY_FINE;
      case LocationEngineRequest.PRIORITY_LOW_POWER:
      case LocationEngineRequest.PRIORITY_NO_POWER:
      default:
        return Criteria.ACCURACY_COARSE;
    }
  }

  private static int priorityToPowerRequirement(int priority) {
    switch (priority) {
      case LocationEngineRequest.PRIORITY_HIGH_ACCURACY:
        return Criteria.POWER_HIGH;
      case LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY:
        return Criteria.POWER_MEDIUM;
      case LocationEngineRequest.PRIORITY_LOW_POWER:
        return Criteria.POWER_LOW;
      case LocationEngineRequest.PRIORITY_NO_POWER:
      default:
        return Criteria.NO_REQUIREMENT;
    }
  }
}
