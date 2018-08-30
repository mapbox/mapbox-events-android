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
  @SerializedName("attachmentMetadata")
  private List<AttachmentMetadata> attachmentMetadata;
  @SerializedName("files")
  private List<byte[]> files;

  Attachment() {
    this.event = VIS_ATTACHMENT;
    attachmentMetadata = new ArrayList<>();
    files = new ArrayList<>();
  }

  protected Attachment(Parcel in) {
    event = in.readString();
    attachmentMetadata = in.createTypedArrayList(AttachmentMetadata.CREATOR);
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

  public void addMetadata(AttachmentMetadata metadata) {
    attachmentMetadata.add(metadata);
  }

  public void addFile(byte[] fileBytes) {
    files.add(fileBytes);
  }

  public List<AttachmentMetadata> getAttachmentMetadata() {
    return attachmentMetadata;
  }

  public List<byte[]> getFiles() {
    return files;
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
    dest.writeTypedList(attachmentMetadata);
  }
}
