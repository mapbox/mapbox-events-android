package com.mapbox.android.core.location;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * The main entry point for location engine integration.
 */
public final class LocationEngineProvider {
  private static final String GOOGLE_LOCATION_SERVICES = "com.google.android.gms.location.LocationServices";

  private LocationEngineProvider() {
    // prevent instantiation
  }

  /**
   * Returns instance to the best location engine, given the included libraries.
   *
   * @param context The {@link Context}.
   * @return a unique instance of {@link LocationEngine} every time method is called.
   */
  @NonNull
  public static LocationEngine getBestLocationEngine(@NonNull Context context) {
    boolean hasGoogleLocationServices = true;
    try {
      Class.forName(GOOGLE_LOCATION_SERVICES);
    } catch (ClassNotFoundException exception) {
      Log.w("LocationEngineProvider", "Missing " + GOOGLE_LOCATION_SERVICES);
      hasGoogleLocationServices = false;
    }

    // Check Google Play services APK is available and up-to-date on this device
    hasGoogleLocationServices &= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            == ConnectionResult.SUCCESS;

    return hasGoogleLocationServices ? new GoogleLocationEngine(context.getApplicationContext()) :
            new AndroidLocationEngine(context.getApplicationContext());
  }
}
