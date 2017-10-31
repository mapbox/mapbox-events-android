package com.mapbox.services.android.telemetry;


import android.util.Log;

@SuppressWarnings("LogNotTimber")
public class Logger {

  int debug(String tag, String msg) {
    return Log.d(tag, msg);
  }
}
