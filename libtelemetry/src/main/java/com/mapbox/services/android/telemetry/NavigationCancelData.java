package com.mapbox.services.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

public class NavigationCancelData implements Parcelable {
  private String arrivalTimestamp;
  private Integer rating = null;
  private String comment = null;

  public NavigationCancelData(String arrivalTimestamp) {
    this.arrivalTimestamp = arrivalTimestamp;
  }

  String getArrivalTimestamp() {
    return arrivalTimestamp;
  }

  Integer getRating() {
    return rating;
  }

  void setRating(Integer rating) {
    this.rating = rating;
  }

  String getComment() {
    return comment;
  }

  void setComment(String comment) {
    this.comment = comment;
  }

  private NavigationCancelData(Parcel in) {
    arrivalTimestamp = in.readString();
    rating = in.readByte() == 0x00 ? null : in.readInt();
    comment = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(arrivalTimestamp);
    if (rating == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(rating);
    }
    dest.writeString(comment);
  }

  @SuppressWarnings("unused")
  public static final Parcelable.Creator<NavigationCancelData> CREATOR =
    new Parcelable.Creator<NavigationCancelData>() {
      @Override
      public NavigationCancelData createFromParcel(Parcel in) {
        return new NavigationCancelData(in);
      }

      @Override
      public NavigationCancelData[] newArray(int size) {
        return new NavigationCancelData[size];
      }
    };
}

