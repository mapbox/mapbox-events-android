package com.mapbox.android.telemetry.location;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.telemetry.LocationEvent;

import java.util.List;

public class LocationUpdatesJobIntentService extends JobIntentService {
  private static final int JOB_ID = 102;
  private static final String TAG = "LocJobIntentService";

  public static void enqueueWork(Context context, Intent intent) {
    enqueueWork(context, LocationUpdatesJobIntentService.class, JOB_ID, intent);
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    try {
      LocationEngineResult result = LocationEngineResult.extractResult(intent);
      if (result == null) {
        Log.w(TAG, "LocationEngineResult == null");
        return;
      }

      List<Location> locations = result.getLocations();
      String sessionId = intent.getStringExtra("session_id");
      for (Location location : locations) {
        if (isThereAnyNaN(location) || isThereAnyInfinite(location)) {
          continue;
        }
        LocationEvent locationEvent = LocationMapper.create(location, sessionId);
        // TODO: push event to repository
      }
    } catch (Throwable throwable) {
      // TODO: log silent crash
    }
  }

  private static boolean isThereAnyNaN(Location location) {
    return Double.isNaN(location.getLatitude()) || Double.isNaN(location.getLongitude())
      || Double.isNaN(location.getAltitude()) || Float.isNaN(location.getAccuracy());
  }

  private static boolean isThereAnyInfinite(Location location) {
    return Double.isInfinite(location.getLatitude()) || Double.isInfinite(location.getLongitude())
      || Double.isInfinite(location.getAltitude()) || Float.isInfinite(location.getAccuracy());
  }
}
