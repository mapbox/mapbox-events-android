package com.mapbox.android.core.location;

import android.app.PendingIntent;

class LocationBroadcastPendingIntent implements LocationPendingIntent {

  private final PendingIntent broadcastPendingIntent;

  LocationBroadcastPendingIntent(PendingIntent broadcastPendingIntent) {
    this.broadcastPendingIntent = broadcastPendingIntent;
  }

  @Override
  public PendingIntent retrievePendingIntent() {
    return broadcastPendingIntent;
  }
}
