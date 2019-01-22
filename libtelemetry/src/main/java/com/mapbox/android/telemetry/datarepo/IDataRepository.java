package com.mapbox.android.telemetry.datarepo;

import android.support.annotation.NonNull;

import com.mapbox.android.telemetry.Event;

import java.util.List;

/**
 * Interface for data repository. The main entry point for caching and getting events data
 */
public interface IDataRepository {
  /**
   * Callback for getting events data
   */
  interface IGetAllEventsCallback {
    /**
     * Data has been prepared for callback
     *
     * @param data all data in data repository, the list should not be null or empty.
     */
    void onDataAvailable(@NonNull List<Event> data);

    /**
     * There is no data in data repository or data could not be returned.
     */
    void onDataNotAvailable();
  }

  /**
   * Push event data to data repository.
   *
   * @param event the event will be cached by data repository.
   */
  void push(@NonNull Event event);

  /**
   * Clear the whole data repository
   */
  void clear();

  /**
   * Get all events data from data repo, when get data from call, all data in this repository will be deleted.
   *
   * @param callback will be called when data is ready or not available.
   */
  void getAllEvents(IGetAllEventsCallback callback);
}
