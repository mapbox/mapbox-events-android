package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class VisionEvent  extends Event implements Parcelable {
  private static final String VIS_GENERAL = "vision.general";

  @SerializedName("event")
  private final String event;
  @SerializedName("name")
  private String name;
  @SerializedName("contents")
  private HashMap<String, Object> contents;

  VisionEvent(String name, HashMap<String, Object> contents) {
    this.event = VIS_GENERAL;
    this.name = name;
    this.contents = contents;
  }

  @Override
  Type obtainType() {
    return Type.VIS_GENERAL;
  }

  private VisionEvent(Parcel in) {
    event = in.readString();
    name = in.readString();
    contents = (HashMap<String, Object>) in.readSerializable();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(event);
    dest.writeString(name);
    dest.writeSerializable(this.contents);
  }

  public static final Creator<VisionEvent> CREATOR = new Creator<VisionEvent>() {
    @Override
    public VisionEvent createFromParcel(Parcel in) {
      return new VisionEvent(in);
    }

    @Override
    public VisionEvent[] newArray(int size) {
      return new VisionEvent[size];
    }
  };
}
