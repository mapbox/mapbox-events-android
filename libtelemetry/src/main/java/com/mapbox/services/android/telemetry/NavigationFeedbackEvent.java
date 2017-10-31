package com.mapbox.services.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;

class NavigationFeedbackEvent extends Event implements Parcelable {
  @JsonAdapter(NavigationMetadataSerializer.class)
  private NavigationMetadata metadata;
  @JsonAdapter(FeedbackEventDataSerializer.class)
  private FeedbackEventData feedbackEventData;
  @JsonAdapter(LocationDataSerializer.class)
  private NavigationLocationData navigationLocationData;
  @JsonAdapter(FeedbackDataSerializer.class)
  private FeedbackData feedbackData;
  private NavigationStepMetadata step = null;

  NavigationFeedbackEvent(NavigationState navigationState) {
    this.metadata = navigationState.getNavigationMetadata();
    this.feedbackEventData = navigationState.getFeedbackEventData();
    this.navigationLocationData = navigationState.getNavigationLocationData();
    this.feedbackData = navigationState.getFeedbackData();
    this.step = navigationState.getNavigationStepMetadata();
  }

  @Override
  Type obtainType() {
    return Type.NAV_FEEDBACK;
  }

  NavigationMetadata getMetadata() {
    return metadata;
  }

  FeedbackEventData getFeedbackEventData() {
    return feedbackEventData;
  }

  NavigationLocationData getNavigationLocationData() {
    return navigationLocationData;
  }

  FeedbackData getFeedbackData() {
    return feedbackData;
  }

  NavigationStepMetadata getStep() {
    return step;
  }

  private NavigationFeedbackEvent(Parcel in) {
    metadata = (NavigationMetadata) in.readValue(NavigationMetadata.class.getClassLoader());
    feedbackEventData = (FeedbackEventData) in.readValue(FeedbackEventData.class.getClassLoader());
    navigationLocationData = (NavigationLocationData) in.readValue(NavigationLocationData.class.getClassLoader());
    feedbackData = (FeedbackData) in.readValue(FeedbackData.class.getClassLoader());
    step = (NavigationStepMetadata) in.readValue(NavigationStepMetadata.class.getClassLoader());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(metadata);
    dest.writeValue(feedbackEventData);
    dest.writeValue(navigationLocationData);
    dest.writeValue(feedbackData);
    dest.writeValue(step);
  }

  @SuppressWarnings("unused")
  public static final Parcelable.Creator<NavigationFeedbackEvent> CREATOR =
    new Parcelable.Creator<NavigationFeedbackEvent>() {
      @Override
      public NavigationFeedbackEvent createFromParcel(Parcel in) {
        return new NavigationFeedbackEvent(in);
      }

      @Override
      public NavigationFeedbackEvent[] newArray(int size) {
        return new NavigationFeedbackEvent[size];
      }
    };
}
