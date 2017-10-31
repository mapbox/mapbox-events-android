package com.mapbox.services.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;

class NavigationDepartEvent extends Event implements Parcelable {
  @JsonAdapter(NavigationMetadataSerializer.class)
  private NavigationMetadata metadata;

  NavigationDepartEvent(NavigationState navigationState) {
    this.metadata = navigationState.getNavigationMetadata();
  }

  @Override
  Type obtainType() {
    return Type.NAV_DEPART;
  }

  NavigationMetadata getMetadata() {
    return metadata;
  }

  private NavigationDepartEvent(Parcel in) {
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
  public static final Creator<NavigationDepartEvent> CREATOR = new Creator<NavigationDepartEvent>() {
    @Override
    public NavigationDepartEvent createFromParcel(Parcel in) {
      return new NavigationDepartEvent(in);
    }

    @Override
    public NavigationDepartEvent[] newArray(int size) {
      return new NavigationDepartEvent[size];
    }
  };
}