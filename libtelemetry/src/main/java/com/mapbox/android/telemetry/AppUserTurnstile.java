package com.mapbox.android.telemetry;


import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import static com.mapbox.android.telemetry.TelemetryEnabler.TELEMETRY_STATES;

@Keep
public class AppUserTurnstile extends Event implements Parcelable {
  private static final String APP_USER_TURNSTILE = "appUserTurnstile";
  private static final String OPERATING_SYSTEM = "Android - " + Build.VERSION.RELEASE;
  private static final String APPLICATION_CONTEXT_CANT_BE_NULL = "Create a MapboxTelemetry instance before calling "
    + "this method.";

  @SerializedName("event")
  private final String event;
  @SerializedName("created")
  private final String created;
  @SerializedName("userId")
  private final String userId;
  @SerializedName("enabled.telemetry")
  private final boolean enabledTelemetry;
  @SerializedName("device")
  private final String device;
  @SerializedName("sdkIdentifier")
  private final String sdkIdentifier;
  @SerializedName("sdkVersion")
  private final String sdkVersion;
  @SerializedName("model")
  private final String model;
  @SerializedName("operatingSystem")
  private final String operatingSystem;
  @SerializedName("skuId")
  private String skuId;

  public AppUserTurnstile(String sdkIdentifier, String sdkVersion) {
    this(sdkIdentifier, sdkVersion, true);
  }

  AppUserTurnstile(String sdkIdentifier, String sdkVersion, boolean isFromPreferences) {
    checkApplicationContext();
    this.event = APP_USER_TURNSTILE;
    this.created = TelemetryUtils.obtainCurrentDate();
    this.userId = TelemetryUtils.retrieveVendorId();
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(isFromPreferences);
    this.enabledTelemetry = TELEMETRY_STATES.get(telemetryEnabler.obtainTelemetryState());
    this.device = Build.DEVICE;
    this.sdkIdentifier = sdkIdentifier;
    this.sdkVersion = sdkVersion;
    this.model = Build.MODEL;
    this.operatingSystem = OPERATING_SYSTEM;
  }

  @Nullable
  public String getSkuId() {
    return skuId;
  }

  public void setSkuId(@NonNull String skuId) {
    if (skuId == null || skuId.length() == 0) {
      return;
    }
    this.skuId = skuId;
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
    device = in.readString();
    sdkIdentifier = in.readString();
    sdkVersion = in.readString();
    model = in.readString();
    operatingSystem = in.readString();
    skuId = in.readString();
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
    dest.writeString(device);
    dest.writeString(sdkIdentifier);
    dest.writeString(sdkVersion);
    dest.writeString(model);
    dest.writeString(operatingSystem);
    dest.writeString(skuId);
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

  private void checkApplicationContext() {
    if (MapboxTelemetry.applicationContext == null) {
      throw new IllegalStateException(APPLICATION_CONTEXT_CANT_BE_NULL);
    }
  }
}