package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

/**
 * A location engine that uses core android.location and has no external dependencies
 * https://developer.android.com/guide/topics/location/strategies.html
 */
class AndroidLocationEngineImpl extends AbstractLocationEngineImpl<LocationListener>
        implements LocationEngineImpl<LocationListener> {
  private static final String TAG = "AndroidLocationEngine";
  private final LocationManager locationManager;

  private String currentProvider = LocationManager.PASSIVE_PROVIDER;

  AndroidLocationEngineImpl(@NonNull Context context) {
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
  }

  @NonNull
  @Override
  LocationListener createListener(final LocationEngineCallback<LocationEngineResult> callback) {
    return new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        callback.onSuccess(LocationEngineResult.create(location));
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
  void destroyListener(@NonNull LocationListener listener) {
    locationManager.removeUpdates(listener);
  }

  @NonNull
  @Override
  public LocationListener getLocationListener(@NonNull LocationEngineCallback<LocationEngineResult> callback) {
    return mapLocationListener(callback);
  }

  @Nullable
  @Override
  public LocationListener removeLocationListener(@NonNull LocationEngineCallback<LocationEngineResult> callback) {
    return unmapLocationListener(callback);
  }

  @Nullable
  @Override
  public LocationEngineResult extractResult(Intent intent) {
    return !hasResult(intent) ? null :
            LocationEngineResult.create((Location)intent.getExtras()
                    .getParcelable(LocationManager.KEY_LOCATION_CHANGED));
  }

  @Override
  public void getLastLocation(@NonNull LocationEngineCallback<LocationEngineResult> callback) throws SecurityException {
    Location lastLocation = null;
    try {
      lastLocation = locationManager.getLastKnownLocation(currentProvider);
    } catch (IllegalArgumentException iae) {
      Log.e(TAG, iae.toString());
    }

    if (lastLocation != null) {
      callback.onSuccess(LocationEngineResult.create(lastLocation));
    } else {
      callback.onFailure(new Exception("Last location unavailable"));
    }
  }

  @Override
  public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                     @NonNull LocationListener listener,
                                     @Nullable Looper looper) throws SecurityException {
    // Pick best provider only if user has not explicitly chosen passive mode
    if (request.getPriority() != LocationEngineRequest.PRIORITY_NO_POWER) {
      currentProvider = locationManager.getBestProvider(getCriteria(request.getPriority()), true);
    }

    locationManager.requestLocationUpdates(currentProvider, request.getInterval(), request.getDisplacemnt(),
            listener, looper);
  }

  @Override
  public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                     @NonNull PendingIntent pendingIntent) throws SecurityException {
    // Pick best provider only if user has not explicitly chosen passive mode
    if (request.getPriority() != LocationEngineRequest.PRIORITY_NO_POWER) {
      currentProvider = locationManager.getBestProvider(getCriteria(request.getPriority()), true);
    }

    locationManager.requestLocationUpdates(currentProvider, request.getInterval(),
            request.getDisplacemnt(), pendingIntent);
  }

  @Override
  public void removeLocationUpdates(@NonNull LocationListener listener) {
    if (listener != null) {
      locationManager.removeUpdates(listener);
    }
  }

  @Override
  public void removeLocationUpdates(@NonNull PendingIntent pendingIntent) {
    if (pendingIntent != null) {
      locationManager.removeUpdates(pendingIntent);
    }
  }

  @VisibleForTesting
  @Override
  public int getListenersCount() {
    return registeredListeners();
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

  private static boolean hasResult(Intent intent) {
    return intent != null && intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED);
  }
}