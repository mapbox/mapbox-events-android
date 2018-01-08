package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

public class FeedbackEventData implements Parcelable {
  private String userId;
  private String feedbackType;
  private String source;
  private String audio;
  private String description = null;

  public FeedbackEventData(String userId, String feedbackType, String source, String audio) {
    this.userId = userId;
    this.feedbackType = feedbackType;
    this.source = source;
    this.audio = audio;
  }

  String getUserId() {
    return userId;
  }

  String getFeedbackType() {
    return feedbackType;
  }

  String getSource() {
    return source;
  }

  String getAudio() {
    return audio;
  }

  String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  private FeedbackEventData(Parcel in) {
    userId = in.readString();
    feedbackType = in.readString();
    source = in.readString();
    audio = in.readString();
    description = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(userId);
    dest.writeString(feedbackType);
    dest.writeString(source);
    dest.writeString(audio);
    dest.writeString(description);
  }

  @SuppressWarnings("unused")
  public static final Parcelable.Creator<FeedbackEventData> CREATOR = new Parcelable.Creator<FeedbackEventData>() {
    @Override
    public FeedbackEventData createFromParcel(Parcel in) {
      return new FeedbackEventData(in);
    }

    @Override
    public FeedbackEventData[] newArray(int size) {
      return new FeedbackEventData[size];
    }
  };
}