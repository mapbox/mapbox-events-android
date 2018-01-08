package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;

class NavigationCancelEvent extends Event implements Parcelable {
  private static final String NAVIGATION_CANCEL = "navigation.cancel";
  private final String event;
  @JsonAdapter(CancelDataSerializer.class)
  private NavigationCancelData cancelData;
  @JsonAdapter(NavigationMetadataSerializer.class)
  private NavigationMetadata metadata;

  NavigationCancelEvent(NavigationState navigationState) {
    this.event = NAVIGATION_CANCEL;
    this.cancelData = navigationState.getNavigationCancelData();
    this.metadata = navigationState.getNavigationMetadata();
  }

  @Override
  Type obtainType() {
    return Type.NAV_CANCEL;
  }

  String getEvent() {
    return event;
  }

  NavigationCancelData getCancelData() {
    return cancelData;
  }

  NavigationMetadata getMetadata() {
    return metadata;
  }

  private NavigationCancelEvent(Parcel in) {
    event = in.readString();
    cancelData = in.readParcelable(NavigationCancelData.class.getClassLoader());
    metadata = in.readParcelable(NavigationMetadata.class.getClassLoader());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(event);
    dest.writeParcelable(cancelData, flags);
    dest.writeParcelable(metadata, flags);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @SuppressWarnings("unused")
  public static final Creator<NavigationCancelEvent> CREATOR = new Creator<NavigationCancelEvent>() {
    @Override
    public NavigationCancelEvent createFromParcel(Parcel in) {
      return new NavigationCancelEvent(in);
    }

    @Override
    public NavigationCancelEvent[] newArray(int size) {
      return new NavigationCancelEvent[size];
    }
  };
}
