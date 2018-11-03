package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.mapbox.android.core.Utils.isBetterLocation;

/**
 * Mapbox replacement for Google Play Services Fused Location Client
 * <p>
 * Note: optimize engine logic for availability
 */
class MapboxFusedLocationEngineImpl extends AndroidLocationEngineImpl {
  private static final String TAG = "MapboxLocationEngine";

  private Location currentBestLocation;
  private LocationListener locationListener;

  MapboxFusedLocationEngineImpl(@NonNull Context context) {
    super(context);
  }

  @NonNull
  @Override
  LocationListener createListener(final LocationEngineCallback<LocationEngineResult> callback) {
    this.locationListener = new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        if (isBetterLocation(location, currentBestLocation)) {
          currentBestLocation = location;
        }

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
    };
    return locationListener;
  }

  @Override
  public void getLastLocation(@NonNull LocationEngineCallback<LocationEngineResult> callback) throws SecurityException {
    Location bestLastLocation = getBestLastLocation();
    if (bestLastLocation != null) {
      callback.onSuccess(LocationEngineResult.create(bestLastLocation));
    } else {
      callback.onFailure(new Exception("Last location unavailable"));
    }
  }

  @Override
  public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                     @NonNull LocationListener listener,
                                     @Nullable Looper looper) throws SecurityException {
    super.requestLocationUpdates(request, listener, looper);

    // Start network provider along with gps
    if (shouldStartNetworkProvider(request.getPriority())) {
      try {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                request.getInterval(), request.getDisplacemnt(),
                listener, looper);
      } catch (IllegalArgumentException iae) {
        iae.printStackTrace();
      }
    }

    // Fetch best last location
    Location bestLastLocation = getBestLastLocation();
    if (bestLastLocation != null) {
      locationListener.onLocationChanged(bestLastLocation);
    }
  }

  @Override
  public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                     @NonNull PendingIntent pendingIntent) throws SecurityException {
    super.requestLocationUpdates(request, pendingIntent);

    // Start network provider along with gps
    if (shouldStartNetworkProvider(request.getPriority())) {
      try {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, request.getInterval(),
                request.getDisplacemnt(), pendingIntent);
      } catch (IllegalArgumentException iae) {
        iae.printStackTrace();
      }
    }

    // TODO: should we broadcast last location?
  }

  @Nullable
  @Override
  public LocationEngineResult extractResult(Intent intent) {
    LocationEngineResult result = super.extractResult(intent);
    if (result == null) {
      return null;
    }

    Location location = result.getLastLocation();
    if (isBetterLocation(location, currentBestLocation)) {
      currentBestLocation = location;
    }
    return LocationEngineResult.create(currentBestLocation);
  }

  private Location getBestLastLocation() {
    Location bestLastLocation = null;
    for (String provider : locationManager.getAllProviders()) {
      Location location = getLastLocationFor(provider);
      if (location == null) {
        continue;
      }

      if (isBetterLocation(location, bestLastLocation)) {
        bestLastLocation = location;
      }
    }
    return bestLastLocation;
  }

  private boolean shouldStartNetworkProvider(int priority) {
    return (priority == LocationEngineRequest.PRIORITY_HIGH_ACCURACY
            || priority == LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            && currentProvider.equals(LocationManager.GPS_PROVIDER);
  }
}
