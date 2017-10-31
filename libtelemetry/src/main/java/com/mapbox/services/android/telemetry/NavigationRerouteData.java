package com.mapbox.services.android.telemetry;

import com.google.gson.annotations.JsonAdapter;

class NavigationRerouteData {
  @JsonAdapter(NewDataSerializer.class)
  private NavigationNewData navigationNewData;
  private int secondsSinceLastReroute;

  NavigationRerouteData(NavigationNewData navigationNewData, int secondsSinceLastReroute) {
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
