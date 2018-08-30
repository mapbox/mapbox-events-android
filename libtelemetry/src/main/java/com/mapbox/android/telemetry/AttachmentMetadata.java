package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

public class AttachmentMetadata extends Event implements Parcelable {

  private String name;
  private String created;
  private String eventId;
  private String format;
  private String type;
  private Integer size;
  private String startTime;
  private String endTime;

  AttachmentMetadata(String name, String eventId, String format, String type) {
    this.name = name;
    this.created = TelemetryUtils.obtainCurrentDate();
    this.eventId = eventId;
    this.format = format;
    this.type = type;
  }

  protected AttachmentMetadata(Parcel in) {
    name = in.readString();
    created = in.readString();
    eventId = in.readString();
    format = in.readString();
    type = in.readString();
    size = in.readByte() == 0x00 ? null : in.readInt();
    startTime = in.readString();
    endTime = in.readString();
  }

  public static final Creator<AttachmentMetadata> CREATOR = new Creator<AttachmentMetadata>() {
    @Override
    public AttachmentMetadata createFromParcel(Parcel in) {
      return new AttachmentMetadata(in);
    }

    @Override
    public AttachmentMetadata[] newArray(int size) {
      return new AttachmentMetadata[size];
    }
  };

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getName() {
    return name;
  }

  public String getCreated() {
    return created;
  }

  public String getEventId() {
    return eventId;
  }

  public String getFormat() {
    return format;
  }

  public String getType() {
    return type;
  }

  public Integer getSize() {
    return size;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  @Override
  Type obtainType() {
    return null;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(created);
    dest.writeString(eventId);
    dest.writeString(format);
    dest.writeString(type);
    if (size == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeInt(size);
    }
    dest.writeString(startTime);
    dest.writeString(endTime);
  }
}
