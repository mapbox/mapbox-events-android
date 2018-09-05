package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Attachment extends Event implements Parcelable {
  private static final String VIS_ATTACHMENT = "vis.attachment";

  @SerializedName("event")
  private final String event;
  @SerializedName("files")
  private List<FileAttachment> attachments;

  Attachment() {
    this.event = VIS_ATTACHMENT;
    this.attachments = new ArrayList<>();
  }

  protected Attachment(Parcel in) {
    event = in.readString();
  }

  public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
    @Override
    public Attachment createFromParcel(Parcel in) {
      return new Attachment(in);
    }

    @Override
    public Attachment[] newArray(int size) {
      return new Attachment[size];
    }
  };

  public List<FileAttachment> getAttachments() {
    return attachments;
  }

  public void addAttachment(FileAttachment attachment) {
    attachments.add(attachment);
  }

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
  }
}
