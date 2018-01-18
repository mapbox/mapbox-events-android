package com.mapbox.android.telemetry;


import android.util.Log;

@SuppressWarnings("LogNotTimber")
class Logger {

  int debug(String tag, String msg) {
    return Log.d(tag, msg);
  }

  int error(String tag, String msg) {
    return Log.e(tag, msg);
  }
}
