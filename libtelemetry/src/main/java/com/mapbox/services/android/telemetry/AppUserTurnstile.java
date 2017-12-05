package com.mapbox.services.android.telemetry;


import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class AppUserTurnstile extends Event implements Parcelable {
  private static final String APP_USER_TURNSTILE = "appUserTurnstile";
  private static final String OPERATING_SYSTEM = "Android - " + Build.VERSION.RELEASE;

  @SerializedName("event")
  private final String event;
  @SerializedName("created")
  private final String created;
  @SerializedName("userId")
  private final String userId;
  @SerializedName("enabled.telemetry")
  private final boolean enabledTelemetry;
  @SerializedName("sdkIdentifier")
  private final String sdkIdentifier;
  @SerializedName("sdkVersion")
  private final String sdkVersion;
  @SerializedName("model")
  private String model = null;
  @SerializedName("operatingSystem")
  private String operatingSystem = null;

  public AppUserTurnstile(boolean enabledTelemetry, String sdkIdentifier, String sdkVersion) {
    this.event = APP_USER_TURNSTILE;
    this.created = TelemetryUtils.obtainCurrentDate();
    this.userId = TelemetryUtils.obtainUniversalUniqueIdentifier();
    this.enabledTelemetry = enabledTelemetry;
    this.sdkIdentifier = sdkIdentifier;
    this.sdkVersion = sdkVersion;
    this.model = Build.MODEL;
    this.operatingSystem = OPERATING_SYSTEM;
  }

  @Override
  Type obtainType() {
    return Type.TURNSTILE;
  }

  private AppUserTurnstile(Parcel in) {
    event = in.readString();
    created = in.readString();
    userId = in.readString();
    enabledTelemetry = in.readByte() != 0x00;
    sdkIdentifier = in.readString();
    sdkVersion = in.readString();
    model = in.readString();
    operatingSystem = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(event);
    dest.writeString(created);
    dest.writeString(userId);
    dest.writeByte((byte) (enabledTelemetry ? 0x01 : 0x00));
    dest.writeString(sdkIdentifier);
    dest.writeString(sdkVersion);
    dest.writeString(model);
    dest.writeString(operatingSystem);
  }

  @SuppressWarnings("unused")
  public static final Parcelable.Creator<AppUserTurnstile> CREATOR = new Parcelable.Creator<AppUserTurnstile>() {
    @Override
    public AppUserTurnstile createFromParcel(Parcel in) {
      return new AppUserTurnstile(in);
    }

    @Override
    public AppUserTurnstile[] newArray(int size) {
      return new AppUserTurnstile[size];
    }
  };
}