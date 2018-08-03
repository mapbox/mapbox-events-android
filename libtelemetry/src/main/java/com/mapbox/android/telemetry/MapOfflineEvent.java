package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class MapOfflineEvent extends Event implements Parcelable {
  private static final String MAP_OFFLINE = "map.offline";

  @SerializedName("event")
  private final String event;
  @SerializedName("created")
  private String created;
  @SerializedName("lat_north")
  private double latitudeNorth;
  @SerializedName("lat_south")
  private double latitudeSouth;
  @SerializedName("lon_east")
  private double longitudeEast;
  @SerializedName("lon_west")
  private double longitudeWest;

  MapOfflineEvent() {
    this.event = MAP_OFFLINE;
    this.created = TelemetryUtils.obtainCurrentDate();
  }

  private MapOfflineEvent(Parcel in) {
    event = in.readString();
    created = in.readString();
    latitudeNorth = in.readDouble();
    latitudeSouth = in.readDouble();
    longitudeEast = in.readDouble();
    longitudeWest = in.readDouble();
  }

  public double getLatitudeNorth() {
    return latitudeNorth;
  }

  public void setLatitudeNorth(double latitudeNorth) {
    this.latitudeNorth = latitudeNorth;
  }

  public double getLatitudeSouth() {
    return latitudeSouth;
  }

  public void setLatitudeSouth(double latitudeSouth) {
    this.latitudeSouth = latitudeSouth;
  }

  public double getLongitudeEast() {
    return longitudeEast;
  }

  public void setLongitudeEast(double longitudeEast) {
    this.longitudeEast = longitudeEast;
  }

  public double getLongitudeWest() {
    return longitudeWest;
  }

  public void setLongitudeWest(double longitudeWest) {
    this.longitudeWest = longitudeWest;
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
    dest.writeDouble(latitudeNorth);
    dest.writeDouble(latitudeSouth);
    dest.writeDouble(longitudeEast);
    dest.writeDouble(longitudeWest);
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
