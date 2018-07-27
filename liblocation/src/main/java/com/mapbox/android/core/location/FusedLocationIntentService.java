package com.mapbox.android.core.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.util.Log;

public class FusedLocationIntentService extends IntentService {

//  public FusedLocationIntentService(String name) {
//    super(name);
//    Log.e("test", "here0");
//  }

  public FusedLocationIntentService() {
    super("");
    Log.e("test", "here1");
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    Log.e("test", "here");
    Log.e("test", "received intent: " + intent);
    Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);

    if (location != null) {
      Log.e("test", "onHandleIntent " + location.getLatitude() + "," + location.getLongitude());
    }
  }
}
