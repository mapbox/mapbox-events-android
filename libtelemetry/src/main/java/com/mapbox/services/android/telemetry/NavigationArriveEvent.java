package com.mapbox.services.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;

class NavigationArriveEvent extends Event implements Parcelable {
  @JsonAdapter(NavigationMetadataSerializer.class)
  private NavigationMetadata metadata;

  NavigationArriveEvent(NavigationState navigationState) {
    this.metadata = navigationState.getNavigationMetadata();
  }

  @Override
  Type obtainType() {
    return Type.NAV_ARRIVE;
  }

  NavigationMetadata getMetadata() {
    return metadata;
  }

  private NavigationArriveEvent(Parcel in) {
    metadata = in.readParcelable(NavigationMetadata.class.getClassLoader());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(metadata, flags);
  }

  @SuppressWarnings("unused")
  public static final Creator<NavigationArriveEvent> CREATOR = new Creator<NavigationArriveEvent>() {
    @Override
    public NavigationArriveEvent createFromParcel(Parcel in) {
      return new NavigationArriveEvent(in);
    }

    @Override
    public NavigationArriveEvent[] newArray(int size) {
      return new NavigationArriveEvent[size];
    }
  };
}
