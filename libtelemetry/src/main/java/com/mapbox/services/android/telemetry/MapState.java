package com.mapbox.services.android.telemetry;

public class MapState {
  private float latitude;
  private float longitude;
  private float zoom;
  private String gesture;

  public MapState(float latitude, float longitude, float zoom) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.zoom = zoom;
  }

  public void setGesture(String gesture) {
    this.gesture = gesture;
  }

  String getGesture() {
    return gesture;
  }

  float getLatitude() {
    return latitude;
  }

  float getLongitude() {
    return longitude;
  }

  float getZoom() {
    return zoom;
  }
}

