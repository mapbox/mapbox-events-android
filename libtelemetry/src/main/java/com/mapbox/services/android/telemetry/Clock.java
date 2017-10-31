package com.mapbox.services.android.telemetry;


import android.os.SystemClock;

class Clock {

  long giveMeTheElapsedRealtime() {
    return SystemClock.elapsedRealtime();
  }
}
