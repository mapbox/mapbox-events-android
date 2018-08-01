package com.mapbox.android.core.location;

import android.app.PendingIntent;

class LocationServicePendingIntent implements LocationPendingIntent {

  private final PendingIntent servicePendingIntent;

  LocationServicePendingIntent(PendingIntent servicePendingIntent) {
    this.servicePendingIntent = servicePendingIntent;
  }

  @Override
  public PendingIntent retrievePendingIntent() {
    return servicePendingIntent;
  }
}
