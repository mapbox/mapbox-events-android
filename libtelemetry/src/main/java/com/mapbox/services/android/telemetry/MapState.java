package com.mapbox.services.android.telemetry;

public class MapState {
  private String gesture;
  private float latitude;
  private float longitude;
  private float zoom;

  public MapState(float latitude, float longitude, float zoom) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.zoom = zoom;
  }

  public void setGesture(String gesture) {
    this.gesture = gesture;
  }

  public String getGesture() {
    return gesture;
  }

  public float getLatitude() {
    return latitude;
  }

  public float getLongitude() {
    return longitude;
  }

  public float getZoom() {
    return zoom;
  }
}

