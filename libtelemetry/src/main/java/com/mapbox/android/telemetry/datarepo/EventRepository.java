package com.mapbox.android.telemetry.datarepo;

import android.support.annotation.NonNull;

import com.mapbox.android.telemetry.Event;

public class EventRepository implements DataSource {
  private static EventRepository instance = new EventRepository();

  private EventRepository() {
  }

  public static EventRepository getInstance() {
    return instance;
  }

  @Override
  public void put(@NonNull Event data) {

  }

  @Override
  public void clear() {

  }

  @Override
  public void getAll(DataSourceCallback callback) {

  }
}
