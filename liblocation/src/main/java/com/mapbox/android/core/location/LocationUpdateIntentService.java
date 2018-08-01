package com.mapbox.android.core.location;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class LocationUpdateIntentService extends IntentService {

  static final String ACTION_PROCESS_UPDATES =
    "com.mapbox.android.core.location.LocationUpdateIntentService.ACTION_PROCESS_UPDATES";
  private static LocationIntentHandler locationIntentHandler;

  public LocationUpdateIntentService() {
    super(LocationUpdateIntentService.class.getSimpleName());
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    locationIntentHandler.handle(intent, ACTION_PROCESS_UPDATES);
  }

  static void addIntentHandler(LocationIntentHandler intentHandler) {
    locationIntentHandler = intentHandler;
  }
}
