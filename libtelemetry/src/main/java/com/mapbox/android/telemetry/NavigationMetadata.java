package com.mapbox.android.telemetry;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class NavigationMetadata implements Parcelable {
  private static final String OPERATING_SYSTEM = "Android - " + Build.VERSION.RELEASE;
  private int absoluteDistanceToDestination;
  private Integer percentTimeInPortrait = null;
  private Integer percentTimeInForeground = null;
  private String startTimestamp;
  private int distanceCompleted;
  private int distanceRemaining;
  private int durationRemaining;
  private String operatingSystem;
  private int eventVersion;
  private String sdKIdentifier;
  private String sdkVersion;
  private String sessionIdentifier;
  private double lat;
  private double lng;
  private String geometry;
  private String created;
  private String profile;
  private Integer estimatedDistance = null;
  private Integer estimatedDuration = null;
  private Integer rerouteCount = null;
  private boolean simulation;
  private String originalRequestIdentifier = null;
  private String requestIdentifier = null;
  private String originalGeometry = null;
  private Integer originalEstimatedDistance = null;
  private Integer originalEstimatedDuration = null;
  private String audioType = null;
  private Integer stepCount = null;
  private Integer originalStepCount = null;
  private String device = "NoDevice";
  private String locationEngine;
  private Integer volumeLevel = null;
  private Integer screenBrightness = null;
  private String applicationState = null;
  private Boolean batteryPluggedIn = null;
  private Integer batteryLevel = null;
  private String connectivity = null;

  public NavigationMetadata(Date startTimestamp, int distanceCompleted, int distanceRemaining, int durationRemaining,
                            String sdKIdentifier, String sdkVersion, int eventVersion, String sessionIdentifier,
                            double lat, double lng, String geometry, String profile, boolean isSimulation,
                            String locationEngine, int absoluteDistanceToDestination) {
    this.startTimestamp = TelemetryUtils.generateCreateDateFormatted(startTimestamp);
    this.distanceCompleted = distanceCompleted;
    this.distanceRemaining = distanceRemaining;
    this.durationRemaining = durationRemaining;
    this.operatingSystem = OPERATING_SYSTEM;
    this.sdKIdentifier = sdKIdentifier;
    this.sdkVersion = sdkVersion;
    this.eventVersion = eventVersion;
    this.sessionIdentifier = sessionIdentifier;
    this.lat = lat;
    this.lng = lng;
    this.geometry = geometry;
    this.created = TelemetryUtils.obtainCurrentDate();
    this.profile = profile;
    this.simulation = isSimulation;
    this.device = Build.MODEL;
    this.locationEngine = locationEngine;
    this.absoluteDistanceToDestination = absoluteDistanceToDestination;
    this.volumeLevel = NavigationUtils.getVolumeLevel();
    this.batteryLevel = TelemetryUtils.getBatteryLevel();
    this.screenBrightness = NavigationUtils.getScreenBrightness();
    this.batteryPluggedIn = TelemetryUtils.isPluggedIn();
    this.connectivity = TelemetryUtils.getCellularNetworkType();
    this.audioType = NavigationUtils.obtainAudioType();
  }

  String getStartTimestamp() {
    return startTimestamp;
  }

  Integer getDistanceCompleted() {
    return distanceCompleted;
  }

  Integer getDistanceRemaining() {
    return distanceRemaining;
  }

  Integer getDurationRemaining() {
    return durationRemaining;
  }

  String getOperatingSystem() {
    return operatingSystem;
  }

  int getEventVersion() {
    return eventVersion;
  }

  String getSdKIdentifier() {
    return sdKIdentifier;
  }

  String getSdkVersion() {
    return sdkVersion;
  }

  String getSessionIdentifier() {
    return sessionIdentifier;
  }

  double getLat() {
    return lat;
  }

  double getLng() {
    return lng;
  }

  String getGeometry() {
    return geometry;
  }

  String getCreated() {
    return created;
  }

  // For testing only
  void setCreated(String created) {
    this.created = created;
  }

  String getProfile() {
    return profile;
  }

  Integer getEstimatedDistance() {
    return estimatedDistance;
  }

  public void setEstimatedDistance(Integer estimatedDistance) {
    this.estimatedDistance = estimatedDistance;
  }

  Integer getEstimatedDuration() {
    return estimatedDuration;
  }

  public void setEstimatedDuration(Integer estimatedDuration) {
    this.estimatedDuration = estimatedDuration;
  }

  Integer getRerouteCount() {
    return rerouteCount;
  }

  public void setRerouteCount(Integer rerouteCount) {
    this.rerouteCount = rerouteCount;
  }

  boolean isSimulation() {
    return simulation;
  }

  String getOriginalRequestIdentifier() {
    return originalRequestIdentifier;
  }

  public void setOriginalRequestIdentifier(String originalRequestIdentifier) {
    this.originalRequestIdentifier = originalRequestIdentifier;
  }

  String getRequestIdentifier() {
    return requestIdentifier;
  }

  public void setRequestIdentifier(String requestIdentifier) {
    this.requestIdentifier = requestIdentifier;
  }

  String getOriginalGeometry() {
    return originalGeometry;
  }

  public void setOriginalGeometry(String originalGeometry) {
    this.originalGeometry = originalGeometry;
  }

  Integer getOriginalEstimatedDistance() {
    return originalEstimatedDistance;
  }

  public void setOriginalEstimatedDistance(Integer originalEstimatedDistance) {
    this.originalEstimatedDistance = originalEstimatedDistance;
  }

  Integer getOriginalEstimatedDuration() {
    return originalEstimatedDuration;
  }

  public void setOriginalEstimatedDuration(Integer originalEstimatedDuration) {
    this.originalEstimatedDuration = originalEstimatedDuration;
  }

  String getAudioType() {
    return audioType;
  }

  public void setAudioType(String audioType) {
    this.audioType = audioType;
  }

  Integer getStepCount() {
    return stepCount;
  }

  public void setStepCount(Integer stepCount) {
    this.stepCount = stepCount;
  }

  Integer getOriginalStepCount() {
    return originalStepCount;
  }

  public void setOriginalStepCount(Integer originalStepCount) {
    this.originalStepCount = originalStepCount;
  }

  String getDevice() {
    return device;
  }

  String getLocationEngine() {
    return locationEngine;
  }

  Integer getVolumeLevel() {
    return volumeLevel;
  }

  Integer getScreenBrightness() {
    return screenBrightness;
  }

  String getApplicationState() {
    return applicationState;
  }

  public void setApplicationState(String applicationState) {
    this.applicationState = applicationState;
  }

  Boolean isBatteryPluggedIn() {
    return batteryPluggedIn;
  }

  Integer getBatteryLevel() {
    return batteryLevel;
  }

  String getConnectivity() {
    return connectivity;
  }

  int getAbsoluteDistanceToDestination() {
    return absoluteDistanceToDestination;
  }

  Integer getPercentTimeInPortrait() {
    return percentTimeInPortrait;
  }

  public void setPercentTimeInPortrait(Integer percentTimeInPortrait) {
    this.percentTimeInPortrait = percentTimeInPortrait;
  }

  Integer getPercentTimeInForeground() {
    return percentTimeInForeground;
  }

  public void setPercentTimeInForeground(Integer percentTimeInForeground) {
    this.percentTimeInForeground = percentTimeInForeground;
  }

  private NavigationMetadata(Parcel in) {
    absoluteDistanceToDestination = in.readInt();
    percentTimeInPortrait = in.readByte() == 0x00 ? null : in.readInt();
    percentTimeInForeground = in.readByte() == 0x00 ? null : in.readInt();
    startTimestamp = in.readString();
    distanceCompleted = in.readInt();
    distanceRemaining = in.readInt();
    durationRemaining = in.readInt();
    operatingSystem = in.readString();
    eventVersion = in.readInt();
    sdKIdentifier = in.readString();
    sdkVersion = in.readString();
    sessionIdentifier = in.readString();
    lat = in.readDouble();
    lng = in.readDouble();
    geometry = in.readString();
    created = in.readString();
    profile = in.readString();
    estimatedDistance = in.readByte() == 0x00 ? null : in.readInt();
    estimatedDuration = in.readByte() == 0x00 ? null : in.readInt();
    rerouteCount = in.readByte() == 0x00 ? null : in.readInt();
    simulation = in.readByte() != 0x00;
    originalRequestIdentifier = in.readString();
    requestIdentifier = in.readString();
    originalGeometry = in.readString();
    originalEstimatedDistance = in.readByte() == 0x00 ? null : in.readInt();
    originalEstimatedDuration = in.readByte() == 0x00 ? null : in.readInt();
    audioType = in.readString();
    stepCount = in.readByte() == 0x00 ? null : in.readInt();
    originalStepCount = in.readByte() == 0x00 ? null : in.readInt();
    device = in.readString();
    locationEngine = in.readString();
    volumeLevel = in.readByte() == 0x00 ? null : in.readInt();
    screenBrightness = in.readByte() == 0x00 ? null : in.readInt();
    applicationState = in.readString();
    byte batteryPluggedInVal = in.readByte();
    batteryPluggedIn = batteryPluggedInVal == 0x02 ? null : batteryPluggedInVal != 0x00;
    batteryLevel = in.readByte() == 0x00 ? null : in.readInt();
    connectivity = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(absoluteDistanceToDestination);
    if (percentTimeInPortrait == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(percentTimeInPortrait);
    }
    if (percentTimeInForeground == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(percentTimeInForeground);
    }
    dest.writeString(startTimestamp);
    dest.writeInt(distanceCompleted);
    dest.writeInt(distanceRemaining);
    dest.writeInt(durationRemaining);
    dest.writeString(operatingSystem);
    dest.writeInt(eventVersion);
    dest.writeString(sdKIdentifier);
    dest.writeString(sdkVersion);
    dest.writeString(sessionIdentifier);
    dest.writeDouble(lat);
    dest.writeDouble(lng);
    dest.writeString(geometry);
    dest.writeString(created);
    dest.writeString(profile);
    if (estimatedDistance == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(estimatedDistance);
    }
    if (estimatedDuration == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(estimatedDuration);
    }
    if (rerouteCount == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(rerouteCount);
    }
    dest.writeByte((byte) (simulation ? 0x01 : 0x00));
    dest.writeString(originalRequestIdentifier);
    dest.writeString(requestIdentifier);
    dest.writeString(originalGeometry);
    if (originalEstimatedDistance == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(originalEstimatedDistance);
    }
    if (originalEstimatedDuration == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(originalEstimatedDuration);
    }
    dest.writeString(audioType);
    if (stepCount == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(stepCount);
    }
    if (originalStepCount == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(originalStepCount);
    }
    dest.writeString(device);
    dest.writeString(locationEngine);
    if (volumeLevel == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(volumeLevel);
    }
    if (screenBrightness == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(screenBrightness);
    }
    dest.writeString(applicationState);
    if (batteryPluggedIn == null) {
      dest.writeByte((byte) (0x02));
    } else {
      dest.writeByte((byte) (batteryPluggedIn ? 0x01 : 0x00));
    }
    if (batteryLevel == null) {
      dest.writeByte((byte) (0x00));
    } else {
      dest.writeByte((byte) (0x01));
      dest.writeInt(batteryLevel);
    }
    dest.writeString(connectivity);
  }

  @SuppressWarnings("unused")
  public static final Parcelable.Creator<NavigationMetadata> CREATOR = new Parcelable.Creator<NavigationMetadata>() {
    @Override
    public NavigationMetadata createFromParcel(Parcel in) {
      return new NavigationMetadata(in);
    }

    @Override
    public NavigationMetadata[] newArray(int size) {
      return new NavigationMetadata[size];
    }
  };
}