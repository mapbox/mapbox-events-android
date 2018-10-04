package com.mapbox.android.core.location;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Sample LocationEngine using Google Play Services
 */
class GoogleLocationEngine extends AbstractLocationEngine<LocationCallback> implements LocationEngine {
  private final FusedLocationProviderClient fusedLocationProviderClient;

  GoogleLocationEngine(@NonNull Context context) {
    super();
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
  }

  @NonNull
  @Override
  protected LocationCallback getListener(final LocationEngineCallback<Location> callback) {
    return new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);

        Location location = locationResult.getLastLocation();
        if (location != null) {
          callback.onSuccess(locationResult.getLastLocation());
        } else {
          callback.onFailure(new Exception("Unavailable location"));
        }
      }
    };
  }

  @Override
  public void getLastLocation(@NonNull final LocationEngineCallback<Location> callback) throws SecurityException {
    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
      @Override
      public void onSuccess(Location location) {
        callback.onSuccess(location);
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
                                     @NonNull LocationEngineCallback<Location> callback,
                                     @Nullable Looper looper) throws SecurityException {
    LocationCallback locationCallback = addLocationListener(callback);
    fusedLocationProviderClient.requestLocationUpdates(toGMSLocationRequest(request), locationCallback, looper);
  }

  @Override
  public void removeLocationUpdates(@NonNull LocationEngineCallback<Location> callback) {
    LocationCallback locationCallback = removeLocationListener(callback);
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
  }

  private static LocationRequest toGMSLocationRequest(LocationEngineRequest request) {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(request.getInterval());
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
