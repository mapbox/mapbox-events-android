package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;


public class MapOfflineEvent extends Event implements Parcelable {

  private static final String MAP_OFFLINE = "map.offline.download";

  @SerializedName("event")
  private final String event;

  @SerializedName("created")
  private final String created;

  @SerializedName("minZoom")
  private Double minZoom;

  @SerializedName("maxZoom")
  private Double maxZoom;

  @SerializedName("shapeForOfflineRegion")
  private String shapeForOfflineRegion;

  @SerializedName("sources")
  private String[] sources;

  public void setMinZoom(Double minZoom) {
    this.minZoom = minZoom;
  }

  public void setMaxZoom(Double maxZoom) {
    this.maxZoom = maxZoom;
  }

  public void setShapeForOfflineRegion(String shapeForOfflineRegion) {
    this.shapeForOfflineRegion = shapeForOfflineRegion;
  }

  public void setSources(String[] sources) {
    this.sources = sources;
  }

  MapOfflineEvent() {
    this.event = MAP_OFFLINE;
    this.created = TelemetryUtils.obtainCurrentDate();
  }

  private MapOfflineEvent(Parcel in) {
    event = in.readString();
    created = in.readString();
    minZoom = in.readDouble();
    maxZoom = in.readDouble();
    shapeForOfflineRegion = in.readString();
    sources = in.createStringArray();
  }

  @Override
  Type obtainType() {
    return Type.MAP_OFFLINE;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(event);
    dest.writeString(created);
    dest.writeDouble(minZoom);
    dest.writeDouble(maxZoom);
    dest.writeString(shapeForOfflineRegion);
    dest.writeStringArray(sources);
  }

  public static final Creator<MapOfflineEvent> CREATOR = new Creator<MapOfflineEvent>() {
    @Override
    public MapOfflineEvent createFromParcel(Parcel in) {
      return new MapOfflineEvent(in);
    }

    @Override
    public MapOfflineEvent[] newArray(int size) {
      return new MapOfflineEvent[size];
    }
  };
}
