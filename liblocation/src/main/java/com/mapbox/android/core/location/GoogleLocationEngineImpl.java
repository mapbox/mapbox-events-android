package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Collections;
import java.util.List;

/**
 * Wraps implementation of Fused Location Provider
 */
class GoogleLocationEngineImpl extends AbstractLocationEngineImpl<LocationCallback>
        implements LocationEngineImpl<LocationCallback> {
  private final FusedLocationProviderClient fusedLocationProviderClient;

  GoogleLocationEngineImpl(@NonNull Context context) {
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
  }

  @NonNull
  @Override
  LocationCallback createListener(final LocationEngineCallback<LocationEngineResult> callback) {
    return new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);

        List<Location> locations = locationResult.getLocations();
        if (!locations.isEmpty()) {
          callback.onSuccess(LocationEngineResult.create(locations));
        } else {
          callback.onFailure(new Exception("Unavailable location"));
        }
      }
    };
  }

  @NonNull
  @Override
  public LocationCallback setLocationListener(@NonNull LocationEngineCallback<LocationEngineResult> callback) {
    return mapLocationListener(callback);
  }

  @Nullable
  @Override
  public LocationCallback removeLocationListener(@NonNull LocationEngineCallback<LocationEngineResult> callback) {
    return unmapLocationListener(callback);
  }

  @Nullable
  @Override
  public LocationEngineResult extractResult(Intent intent) {
    LocationResult result = LocationResult.extractResult(intent);
    return result != null ? LocationEngineResult.create(result.getLocations()) : null;
  }

  @Override
  public void getLastLocation(@NonNull final LocationEngineCallback<LocationEngineResult> callback)
          throws SecurityException {
    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
      @Override
      public void onSuccess(Location location) {
        callback.onSuccess(location != null ? LocationEngineResult.create(location) :
                LocationEngineResult.create(Collections.EMPTY_LIST));
      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        callback.onFailure(e);
      }
    });
  }

  @Override
  public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                     @NonNull LocationCallback listener,
                                     @Nullable Looper looper) throws SecurityException {
    fusedLocationProviderClient.requestLocationUpdates(toGMSLocationRequest(request), listener, looper);
  }

  @Override
  public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                     @NonNull PendingIntent pendingIntent) throws SecurityException {
    fusedLocationProviderClient.requestLocationUpdates(toGMSLocationRequest(request), pendingIntent);
  }

  @Override
  public void removeLocationUpdates(@NonNull LocationCallback listener) {
    if (listener != null) {
      fusedLocationProviderClient.removeLocationUpdates(listener);
    }
  }

  @Override
  public void removeLocationUpdates(@NonNull PendingIntent pendingIntent) {
    if (pendingIntent != null) {
      fusedLocationProviderClient.removeLocationUpdates(pendingIntent);
    }
  }

  @VisibleForTesting
  @Override
  public int getListenersCount() {
    return registeredListeners();
  }

  private static LocationRequest toGMSLocationRequest(LocationEngineRequest request) {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(request.getInterval());
    locationRequest.setFastestInterval(request.getFastestInterval());
    locationRequest.setSmallestDisplacement(request.getDisplacemnt());
    locationRequest.setMaxWaitTime(request.getMaxWaitTime());
    locationRequest.setPriority(toGMSLocationPriority(request.getPriority()));
    return locationRequest;
  }

  private static int toGMSLocationPriority(int enginePriority) {
    switch (enginePriority) {
      case LocationEngineRequest.PRIORITY_HIGH_ACCURACY:
        return LocationRequest.PRIORITY_HIGH_ACCURACY;
      case LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY:
        return LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
      case LocationEngineRequest.PRIORITY_LOW_POWER:
        return LocationRequest.PRIORITY_LOW_POWER;
      case LocationEngineRequest.PRIORITY_NO_POWER:
      default:
        return LocationRequest.PRIORITY_NO_POWER;
    }
  }
}
