package com.mapbox.android.core.location;

import android.content.Context;
import android.location.Location;

import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample LocationEngine using Google Play Services
 */
class GoogleLocationEngine extends LocationEngine {

  private FusedLocationProviderClient fusedLocationProviderClient;
  private WeakReference<Context> context;
  private final Map<LocationEnginePriority, UpdateGoogleRequestPriority> REQUEST_PRIORITY = new
    HashMap<LocationEnginePriority, UpdateGoogleRequestPriority>() {
      {
        put(LocationEnginePriority.NO_POWER, new UpdateGoogleRequestPriority() {
          @Override
          public void update(LocationRequest request) {
            request.setPriority(LocationRequest.PRIORITY_NO_POWER);
          }
        });
        put(LocationEnginePriority.LOW_POWER, new UpdateGoogleRequestPriority() {
          @Override
          public void update(LocationRequest request) {
            request.setPriority(LocationRequest.PRIORITY_LOW_POWER);
          }
        });
        put(LocationEnginePriority.BALANCED_POWER_ACCURACY, new UpdateGoogleRequestPriority() {
          @Override
          public void update(LocationRequest request) {
            request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
          }
        });
        put(LocationEnginePriority.HIGH_ACCURACY, new UpdateGoogleRequestPriority() {
          @Override
          public void update(LocationRequest request) {
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
          }
        });
      }
    };

  private GoogleLocationEngine(Context context) {
    super();
    this.context = new WeakReference<>(context);
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.context.get());
  }

  static synchronized LocationEngine getLocationEngine(Context context) {
    LocationEngine googleLocationEngine = new GoogleLocationEngine(context.getApplicationContext());

    return googleLocationEngine;
  }

  @Override
  public void activate() {
    for (LocationEngineListener listener : locationListeners) {
      listener.onConnected();
    }
  }

  @Override
  public void deactivate() {

  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public void requestLastLocation(final LastLocationListener lastLocationListener) {
    //noinspection MissingPermission
    fusedLocationProviderClient.getLastLocation()
      .addOnSuccessListener(new OnSuccessListener<Location>() {
        @Override
        public void onSuccess(Location location) {
          lastLocationListener.onLastLocationSuccess(location);
        }
      })
      .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
          lastLocationListener.onLastLocationFail(exception);
        }
      });
  }

  @Deprecated
  @Override
  public Location getLastLocation() {
    // No op
    //noinspection MissingPermission
    return null;
  }

  @Override
  public void requestLocationUpdates() {
    LocationRequest request = LocationRequest.create();

    if (interval != null) {
      request.setInterval(interval);
    }
    if (fastestInterval != null) {
      request.setFastestInterval(fastestInterval);
    }
    if (smallestDisplacement != null) {
      request.setSmallestDisplacement(smallestDisplacement);
    }

    updateRequestPriority(request);

    //noinspection MissingPermission
    fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
  }

  @Override
  public void removeLocationUpdates() {
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
  }

  @Override
  public Type obtainType() {
    return Type.GOOGLE_PLAY_SERVICES;
  }

  private LocationCallback locationCallback = new LocationCallback() {
    @Override
    public void onLocationResult(LocationResult locationResult) {
      List<Location> locationList = locationResult.getLocations();
      if (!locationList.isEmpty()) {
        int lastLocation = locationList.size() - 1;
        Location location = locationList.get(lastLocation);
        for (LocationEngineListener listener : locationListeners) {
          listener.onLocationChanged(location);
        }
      }
    }
  };

  private void updateRequestPriority(LocationRequest request) {
    REQUEST_PRIORITY.get(priority).update(request);
  }
}
