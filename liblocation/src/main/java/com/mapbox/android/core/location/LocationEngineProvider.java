package com.mapbox.android.core.location;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mapbox.android.core.location.LocationEnginePriority.BALANCED_POWER_ACCURACY;
import static com.mapbox.android.core.location.LocationEnginePriority.HIGH_ACCURACY;

public class LocationEngineProvider {
  private Map<LocationEngine.Type, LocationEngine> locationEngineDictionary;
  private static final List<LocationEngine.Type> OPTIONAL_LOCATION_ENGINES = new ArrayList<LocationEngine.Type>() {
    {
      add(LocationEngine.Type.GOOGLE_PLAY_SERVICES);
    }
  };
  private int googlePlayAvailabilityResultCode;

  public LocationEngineProvider(Context context) {
    googlePlayAvailabilityResultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context.getApplicationContext());
    initAvailableLocationEngines(context);
  }

  /**
   * Get the best location engine, given the included libraries
   *
   * @return a unique instance of {@link LocationEngine} every time method is called.
   */
  @NonNull
  public LocationEngine obtainBestLocationEngineAvailable() {
    return obtainBestLocationEngine();
  }

  /**
   * Get a location engine of desired type
   *
   * @param type {@link LocationEngine.Type}
   * @return a unique instance of {@link LocationEngine} every time method is called.
   */
  @Nullable
  public LocationEngine obtainLocationEngineBy(LocationEngine.Type type) {
    LocationEngine locationEngine = locationEngineDictionary.get(type);
    return locationEngine;
  }

  private void initAvailableLocationEngines(Context context) {
    locationEngineDictionary = new HashMap<>();
    Map<LocationEngine.Type, LocationEngineSupplier> locationEnginesDictionary =
      obtainDefaultLocationEnginesDictionary();
    for (Map.Entry<LocationEngine.Type, LocationEngineSupplier> entry : locationEnginesDictionary.entrySet()) {
      LocationEngineSupplier locationEngineSupplier = entry.getValue();
      if (locationEngineSupplier.hasDependencyOnClasspath()) {
        LocationEngine available = locationEngineSupplier.supply(context);
        locationEngineDictionary.put(entry.getKey(), available);
      }
    }
  }

  private Map<LocationEngine.Type, LocationEngineSupplier> obtainDefaultLocationEnginesDictionary() {
    ClasspathChecker classpathChecker = new ClasspathChecker();
    Map<LocationEngine.Type, LocationEngineSupplier> locationSources = new HashMap<>();
    locationSources.put(LocationEngine.Type.GOOGLE_PLAY_SERVICES, new GoogleLocationEngineFactory(classpathChecker));
    locationSources.put(LocationEngine.Type.ANDROID, new AndroidLocationEngineFactory());

    return locationSources;
  }

  private LocationEngine obtainBestLocationEngine() {
    LocationEngine bestLocationEngine;

    // Check to see if google play services is installed and updated to required version
    if (googlePlayAvailabilityResultCode == ConnectionResult.SUCCESS) {
      bestLocationEngine = getGoogleLocationEngine();
    } else {
      bestLocationEngine = getAndroidLocationEngine();
    }
    return bestLocationEngine;
  }

  private LocationEngine getAndroidLocationEngine() {
    LocationEngine androidLocationEngine;
    androidLocationEngine = obtainLocationEngineBy(LocationEngine.Type.ANDROID);
    if (androidLocationEngine != null) {
      androidLocationEngine.setPriority(BALANCED_POWER_ACCURACY);
      androidLocationEngine.setFastestInterval(5000);
    }
    return androidLocationEngine;
  }

  private LocationEngine getGoogleLocationEngine() {
    LocationEngine googleLocationEngine;
    googleLocationEngine = obtainLocationEngineBy(LocationEngine.Type.GOOGLE_PLAY_SERVICES);
    if (googleLocationEngine != null) {
      googleLocationEngine.setPriority(HIGH_ACCURACY);
      googleLocationEngine.setFastestInterval(2000);
    }
    return googleLocationEngine;
  }
}
