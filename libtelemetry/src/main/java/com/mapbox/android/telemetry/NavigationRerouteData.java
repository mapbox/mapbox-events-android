package com.mapbox.android.telemetry;

import com.google.gson.annotations.JsonAdapter;

public class NavigationRerouteData {
  @JsonAdapter(NewDataSerializer.class)
  private NavigationNewData navigationNewData;
  private int secondsSinceLastReroute;

  public NavigationRerouteData(NavigationNewData navigationNewData, int secondsSinceLastReroute) {
    this.navigationNewData = navigationNewData;
    this.secondsSinceLastReroute = secondsSinceLastReroute;
  }

  NavigationNewData getNavigationNewData() {
    return navigationNewData;
  }

  Integer getSecondsSinceLastReroute() {
    return secondsSinceLastReroute;
  }
}
