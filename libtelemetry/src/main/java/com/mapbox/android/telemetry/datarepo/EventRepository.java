package com.mapbox.android.telemetry.datarepo;

import android.support.annotation.NonNull;

import com.mapbox.android.telemetry.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that cache event data.
 */
public class EventRepository implements DataSource {
  private List<Event> list = Collections.synchronizedList(new ArrayList<Event>());
  private static EventRepository instance = new EventRepository();

  private EventRepository() {
  }

  public static EventRepository getInstance() {
    return instance;
  }

  @Override
  public void put(@NonNull Event data) {
    list.add(data);
    //todo save data to database.
  }

  @Override
  public void clear() {
    list.clear();
  }

  @Override
  public void getAll(DataSourceCallback callback) {
    List<Event> tempList = list;
    list = Collections.synchronizedList(new ArrayList<Event>());
    callback.onDataAvailable(tempList);
  }
}
