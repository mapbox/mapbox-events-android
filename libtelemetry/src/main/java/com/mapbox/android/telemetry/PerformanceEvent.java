package com.mapbox.android.telemetry;

import android.os.Bundle;
import android.os.Parcel;

import com.google.gson.annotations.SerializedName;


public class PerformanceEvent extends Event {

  private static final String PERFORMANCE_TRACE = "performance.trace";

  @SerializedName("event")
  private final String event;

  @SerializedName("created")
  private final String created;

  @SerializedName("sessionId")
  private final String sessionId;

  @SerializedName("data")
  private final Bundle data;

  PerformanceEvent(String sessionId, Bundle data) {
    this.event = PERFORMANCE_TRACE;
    this.created = TelemetryUtils.obtainCurrentDate();
    this.sessionId = sessionId;
    this.data = data;
  }

  private PerformanceEvent(Parcel in) {
    this.event = in.readString();
    this.created = in.readString();
    this.sessionId = in.readString();
    this.data = in.readBundle();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(event);
    parcel.writeString(created);
    parcel.writeString(sessionId);
    parcel.writeBundle(data);
  }

  public static final Creator<PerformanceEvent> CREATOR = new Creator<PerformanceEvent>() {
    @Override
    public PerformanceEvent createFromParcel(Parcel in) {
      return new PerformanceEvent(in);
    }

    @Override
    public PerformanceEvent[] newArray(int size) {
      return new PerformanceEvent[size];
    }
  };
}
