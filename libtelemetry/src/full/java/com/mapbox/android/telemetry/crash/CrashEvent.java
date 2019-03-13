package com.mapbox.android.telemetry.crash;

import android.annotation.SuppressLint;
import android.os.Parcel;
import com.mapbox.android.telemetry.Event;

@SuppressLint("ParcelCreator")
class CrashEvent extends Event {
  String getHash() {
    return null;
  }

  boolean isValid() {
    return false;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    // no-op
  }
}
