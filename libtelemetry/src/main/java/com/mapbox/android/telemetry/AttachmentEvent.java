package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class AttachmentEvent extends Event implements Parcelable {
  private static final String ATTACHMENT = "attachment";

  @SerializedName("event")
  private final String event;
  @SerializedName("created")
  private final String created;
  @SerializedName("userId")
  private final String userId;
  @SerializedName("format")
  private final String format;
  @SerializedName("type")
  private final String type;
  @SerializedName("size")
  private final Integer size;
  @SerializedName("startTime")
  private final String startTime;
  @SerializedName("endTime")
  private final String endTime;

  AttachmentEvent(String format, String type, int size, String startTime, String endTime) {
    this.event = ATTACHMENT;
    this.created = TelemetryUtils.obtainCurrentDate();
    this.userId = TelemetryUtils.retrieveVendorId();
    this.format = format;
    this.type = type;
    this.size = size;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  protected AttachmentEvent(Parcel in) {
    event = in.readString();
    created = in.readString();
    userId = in.readString();
    format = in.readString();
    type = in.readString();
    size = in.readInt();
    startTime = in.readString();
    endTime = in.readString();
  }

  public static final Creator<AttachmentEvent> CREATOR = new Creator<AttachmentEvent>() {
    @Override
    public AttachmentEvent createFromParcel(Parcel in) {
      return new AttachmentEvent(in);
    }

    @Override
    public AttachmentEvent[] newArray(int size) {
      return new AttachmentEvent[size];
    }
  };

  @Override
  Type obtainType() {
    return Type.VIS_ATTACHMENT;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(event);
    dest.writeString(created);
    dest.writeString(userId);
    dest.writeString(format);
    dest.writeString(type);
    dest.writeInt(size);
    dest.writeString(startTime);
    dest.writeString(endTime);
  }
}
