package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.VisibleForTesting;
import com.google.gson.annotations.SerializedName;

/**
 * Vision object detection event v2.
 */
public class VisionObjectDetectionEvent extends Event implements Parcelable {
  @VisibleForTesting
  static final String VIS_OBJECT_DETECTION = "vision.objectDetection";

  // Mandatory attributes
  @SerializedName("event")
  private final String event;
  @SerializedName("created")
  private final String created;

  // Optional attributes
  @SerializedName("object_lat")
  private Double object_lat;
  @SerializedName("object_lon")
  private Double object_lon;
  @SerializedName("vehicle_lat")
  private Double vehicle_lat;
  @SerializedName("vehicle_lon")
  private Double vehicle_lon;
  @SerializedName("class")
  private String clazz;
  @SerializedName("sign_value")
  private String sign_value;
  @SerializedName("object_size_width")
  private Double object_size_width;
  @SerializedName("object_size_height")
  private Double object_size_height;
  @SerializedName("object_pos_height")
  private Double object_pos_height;
  @SerializedName("distance_from_camera")
  private Double distance_from_camera;

  public VisionObjectDetectionEvent(String created) {
    this.event = VIS_OBJECT_DETECTION;
    this.created = created;
    this.object_lat = null;
    this.object_lon = null;
    this.vehicle_lat = null;
    this.vehicle_lon = null;
    this.clazz = null;
    this.sign_value = null;
    this.object_size_height = null;
    this.object_size_width = null;
    this.object_pos_height = null;
    this.distance_from_camera = null;
  }

  public String getEvent() {
    return event;
  }

  public String getCreated() {
    return created;
  }

  public double getObjectLatitude() {
    return object_lat;
  }

  public void setObjectLatitude(double latitude) {
    this.object_lat = latitude;
  }

  public double getObjectLongitude() {
    return object_lon;
  }

  public void setObjectLongitude(double longitude) {
    this.object_lon = longitude;
  }

  public double getVehicleLatitude() {
    return vehicle_lat;
  }

  public void setVehicleLatitude(double latitude) {
    this.vehicle_lat = latitude;
  }

  public double getVehicleLongitude() {
    return vehicle_lon;
  }

  public void setVehicleLongitude(double longitude) {
    this.vehicle_lon = longitude;
  }

  public String getClazz() {
    return clazz;
  }

  public void setClazz(String clazz) {
    this.clazz = clazz;
  }

  public String getSignValue() {
    return sign_value;
  }

  public void setSignValue(String sign_value) {
    this.sign_value = sign_value;
  }

  public double getObjectSizeWidth() {
    return object_size_width;
  }

  public void setObjectSizeWidth(double object_size_width) {
    this.object_size_width = object_size_width;
  }

  public double getObjectSizeHeight() {
    return object_size_height;
  }

  public void setObjectSizeHeight(double object_size_height) {
    this.object_size_height = object_size_height;
  }

  public double getObjectPositionHeight() {
    return object_pos_height;
  }

  public void setObjectPositionHeight(double object_pos_height) {
    this.object_pos_height = object_pos_height;
  }

  public double getDistanceFromCamera() {
    return distance_from_camera;
  }

  public void setDistanceFromCamera(double distance_from_camera) {
    this.distance_from_camera = distance_from_camera;
  }

  @Override
  Event.Type obtainType() {
    return Type.VIS_OBJ_DETECTION;
  }

  private VisionObjectDetectionEvent(Parcel in) {
    this.event = in.readString();
    this.created = in.readString();
    this.object_lat = readDoubleIfNotNull(in);
    this.object_lon = readDoubleIfNotNull(in);
    this.vehicle_lat = readDoubleIfNotNull(in);
    this.vehicle_lon = readDoubleIfNotNull(in);
    this.clazz = readStringIfNotNull(in);
    this.sign_value = readStringIfNotNull(in);
    this.object_size_width = readDoubleIfNotNull(in);
    this.object_size_height = readDoubleIfNotNull(in);
    this.object_pos_height = readDoubleIfNotNull(in);
    this.distance_from_camera = readDoubleIfNotNull(in);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(event);
    dest.writeString(created);

    writeDoubleIfNotNull(dest, object_lat);
    writeDoubleIfNotNull(dest, object_lon);
    writeDoubleIfNotNull(dest, vehicle_lat);
    writeDoubleIfNotNull(dest, vehicle_lon);
    writeStringIfNotNull(dest, clazz);
    writeStringIfNotNull(dest, sign_value);
    writeDoubleIfNotNull(dest, object_size_width);
    writeDoubleIfNotNull(dest, object_size_height);
    writeDoubleIfNotNull(dest, object_pos_height);
    writeDoubleIfNotNull(dest, distance_from_camera);
  }

  private static void writeDoubleIfNotNull(Parcel parcel, Double value) {
    parcel.writeByte((byte) (value != null ? 1 : 0));
    if (value != null) {
      parcel.writeDouble(value);
    }
  }

  private static void writeStringIfNotNull(Parcel parcel, String value) {
    parcel.writeByte((byte) (value != null ? 1 : 0));
    if (value != null) {
      parcel.writeString(value);
    }
  }

  private static Double readDoubleIfNotNull(Parcel parcel) {
    return parcel.readByte() == 0 ? null : parcel.readDouble();
  }

  private static String readStringIfNotNull(Parcel parcel) {
    return parcel.readByte() == 0 ? null : parcel.readString();
  }

  public static final Parcelable.Creator<VisionObjectDetectionEvent> CREATOR =
    new Parcelable.Creator<VisionObjectDetectionEvent>() {
      @Override
      public VisionObjectDetectionEvent createFromParcel(Parcel in) {
        return new VisionObjectDetectionEvent(in);
      }

      @Override
      public VisionObjectDetectionEvent[] newArray(int size) {
        return new VisionObjectDetectionEvent[size];
      }
    };
}
