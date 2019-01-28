package com.mapbox.android.telemetry.datarepo;

import android.support.annotation.NonNull;

import com.mapbox.android.telemetry.Event;

import java.util.List;


/**
 * Interface for data repository. The main entry point for caching and getting data
 */
public interface DataSource {
  /**
   * Callback for getting data
   */
  interface DataSourceCallback {
    /**
     * Data has been prepared for callback
     *
     * @param data all data in data repository, the list should not be null or empty.
     */
    void onDataAvailable(@NonNull List<Event> data);
  }

  /**
   * Cache data to data repository.
   *
   * @param data the data will be cached by data repository.
   */
  void put(@NonNull Event data);

  /**
   * Clear the whole data repository
   */
  void clear();

  /**
   * Get all data from data repo, when get data from call, all data in this repository will be deleted.
   *
   * @param callback will be called when data is ready or not available.
   */
  void getAll(DataSourceCallback callback);
}
