package com.mapbox.android.core.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.mapbox.android.core.permissions.PermissionsManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * A location engine that uses core android.location and has no external dependencies
 * https://developer.android.com/guide/topics/location/strategies.html
 */
class AndroidLocationEngine extends LocationEngine implements LocationListener {

  private static final String DEFAULT_PROVIDER = LocationManager.PASSIVE_PROVIDER;

  private WeakReference<Context> context;
  private LocationManager locationManager;
  private String currentProvider = null;
  private final Map<LocationEnginePriority, UpdateAndroidProvider> CURRENT_PROVIDER = new
    HashMap<LocationEnginePriority, UpdateAndroidProvider>() {
      {
        put(LocationEnginePriority.NO_POWER, new UpdateAndroidProvider() {
          @Override
          public void update() {
            currentProvider = LocationManager.PASSIVE_PROVIDER;
          }
        });
        put(LocationEnginePriority.LOW_POWER, new UpdateAndroidProvider() {
          @Override
          public void update() {
            currentProvider = LocationManager.NETWORK_PROVIDER;
          }
        });
        put(LocationEnginePriority.BALANCED_POWER_ACCURACY, new UpdateAndroidProvider() {
          @Override
          public void update() {
            currentProvider = LocationManager.NETWORK_PROVIDER;
          }
        });
        put(LocationEnginePriority.HIGH_ACCURACY, new UpdateAndroidProvider() {
          @Override
          public void update() {
            currentProvider = LocationManager.GPS_PROVIDER;
          }
        });
      }
    };

  private AndroidLocationEngine(Context context) {
    super();

    this.context = new WeakReference<>(context);
    locationManager = (LocationManager) this.context.get().getSystemService(Context.LOCATION_SERVICE);
    currentProvider = DEFAULT_PROVIDER;
  }

  static synchronized LocationEngine getLocationEngine(Context context) {
    AndroidLocationEngine androidLocationEngine = new AndroidLocationEngine(context.getApplicationContext());

    return androidLocationEngine;
  }

  @Override
  public void activate() {
    // "Connection" is immediate
    for (LocationEngineListener listener : locationListeners) {
      listener.onConnected();
    }
  }

  @Override
  public void deactivate() {
    // No op
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public Location getLastLocation() {
    if (!TextUtils.isEmpty(currentProvider)) {
      //noinspection MissingPermission
      return locationManager.getLastKnownLocation(currentProvider);
    }

    //noinspection MissingPermission
    return null;
  }

  @Override
  public void requestLocationUpdates() {
    if (!TextUtils.isEmpty(currentProvider)) {
      //noinspection MissingPermission
      locationManager.requestLocationUpdates(currentProvider, fastestInterval, smallestDisplacement, this);
    }
  }

  @Override
  public void setPriority(LocationEnginePriority priority) {
    super.setPriority(priority);
    updateCurrentProvider();
  }

  @Override
  public void removeLocationUpdates() {
    if (PermissionsManager.areLocationPermissionsGranted(context.get())) {
      //noinspection MissingPermission
      locationManager.removeUpdates(this);
    }
  }

  @Override
  public Type obtainType() {
    return Type.ANDROID;
  }

  /**
   * Called when the location has changed.
   */
  @Override
  public void onLocationChanged(Location location) {
    for (LocationEngineListener listener : locationListeners) {
      listener.onLocationChanged(location);
    }
  }

  /**
   * Called when the provider status changes.
   */
  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
  }

  /**
   * Called when the provider is enabled by the user.
   */
  @Override
  public void onProviderEnabled(String provider) {
  }

  /**
   * Called when the provider is disabled by the user.
   */
  @Override
  public void onProviderDisabled(String provider) {
  }

  private void updateCurrentProvider() {
    // We might want to explore android.location.Criteria here.
    CURRENT_PROVIDER.get(priority).update();
  }
}
