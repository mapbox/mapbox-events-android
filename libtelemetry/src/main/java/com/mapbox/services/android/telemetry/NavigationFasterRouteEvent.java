package com.mapbox.services.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;

class NavigationFasterRouteEvent extends Event implements Parcelable {
  @JsonAdapter(NavigationMetadataSerializer.class)
  private NavigationMetadata metadata = null;
  @JsonAdapter(NewDataSerializer.class)
  private NavigationNewData navigationNewData = null;
  private NavigationStepMetadata step = null;

  NavigationFasterRouteEvent(NavigationState navigationState) {
    NavigationRerouteData navigationRerouteData = navigationState.getNavigationRerouteData();
    this.navigationNewData = navigationRerouteData.getNavigationNewData();
    this.step = navigationState.getNavigationStepMetadata();
    this.metadata = navigationState.getNavigationMetadata();
  }

  @Override
  Type obtainType() {
    return Type.NAV_REROUTE;
  }

  NavigationNewData getNavigationNewData() {
    return navigationNewData;
  }

  NavigationStepMetadata getStep() {
    return step;
  }

  NavigationMetadata getMetadata() {
    return metadata;
  }

  private NavigationFasterRouteEvent(Parcel in) {
    navigationNewData = in.readParcelable(NavigationNewData.class.getClassLoader());
    step = in.readParcelable(NavigationStepMetadata.class.getClassLoader());
    metadata = in.readParcelable(NavigationMetadata.class.getClassLoader());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(navigationNewData, flags);
    dest.writeParcelable(step, flags);
    dest.writeParcelable(metadata, flags);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @SuppressWarnings("unused")
  public static final Creator<NavigationFasterRouteEvent> CREATOR = new Creator<NavigationFasterRouteEvent>() {
    @Override
    public NavigationFasterRouteEvent createFromParcel(Parcel in) {
      return new NavigationFasterRouteEvent(in);
    }

    @Override
    public NavigationFasterRouteEvent[] newArray(int size) {
      return new NavigationFasterRouteEvent[size];
    }
  };
}
