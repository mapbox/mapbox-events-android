package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@SuppressLint("ParcelCreator")
@Keep
class KeyValue implements Parcelable {
  @SerializedName("name")
  private final String name;
  @SerializedName("value")
  private final String value;

  public KeyValue(String name, String value) {
    this.name = name;
    this.value = value;
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
