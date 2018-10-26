package com.mapbox.android.core.location;

import android.location.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mapbox.android.core.location.Utils.checkNotNull;

/**
 * A wrapper class representing location result from the location engine.
 * <p>
 * TODO: Override default equals(), hashCode() and toString()
 *
 * @since 3.0.0
 */
public final class LocationEngineResult {
  private final List<Location> locations;

  private LocationEngineResult(List<Location> locations) {
    this.locations = Collections.unmodifiableList(locations);
  }

  /**
   * Creates {@link LocationEngineResult} instance for location.
   *
   * @param location default location added to the result.
   * @return instance of the new location result.
   * @since 3.0.0
   */
  public static LocationEngineResult create(Location location) {
    checkNotNull(location, "location can't be null");
    List<Location> locations = new ArrayList<>();
    locations.add(location);
    return new LocationEngineResult(locations);
  }

  /**
   * Creates {@link LocationEngineResult} instance for given list of locations.
   *
   * @param locations list of locations.
   * @return instance of the new location result.
   * @since 3.0.0
   */
  public static LocationEngineResult create(List<Location> locations) {
    checkNotNull(locations, "locations can't be null");
    return new LocationEngineResult(locations);
  }

  /**
   * Returns most recent location available in this result.
   *
   * @return the most recent location {@link Location} or null.
   * @since 3.0.0
   */
  public Location getLastLocation() {
    return locations.isEmpty() ? null : locations.get(0);
  }

  /**
   * Returns locations computed, ordered from oldest to newest.
   *
   * @return ordered list of locations.
   * @since 3.0.0
   */
  public List<Location> getLocations() {
    return Collections.unmodifiableList(locations);
  }
}
