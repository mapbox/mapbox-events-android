package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * This class is temporary and exists only
 * to comply with legacy telemetry interface,
 * we should open up a way to send raw json data to
 * the telemetry endpoint in the future to avoid,
 * back and forth json serialization overhead.
 */
@SuppressLint("ParcelCreator")
@Keep
public class CrashEvent extends Event {
  @SerializedName("event")
  private final String event;
  @SerializedName("created")
  private final String created;

  @SerializedName("sdkIdentifier")
  private String sdkIdentifier;
  @SerializedName("sdkVersion")
  private String sdkVersion;
  @SerializedName("osVersion")
  private String osVersion;
  @SerializedName("model")
  private String model;
  @SerializedName("device")
  private String device;
  @SerializedName("isSilent")
  private String isSilent;
  @SerializedName("stackTraceHash")
  private String stackTraceHash;
  @SerializedName("stackTrace")
  private String stackTrace;
  @SerializedName("threadDetails")
  private String threadDetails;
  @SerializedName("appId")
  private String appId;
  @SerializedName("appVersion")
  private String appVersion;
  @SerializedName("customData")
  private List<KeyValue> customData;

  public CrashEvent(String event, String created) {
    this.event = event;
    this.created = created;
  }

  /**
   * Unfortunately, we have no choice but
   * to override deprecated method to dispatch
   * crash events immediately for delivery to
   * the telemetry endpoint
   *
   * @return crash event type.
   */
  @Override
  Type obtainType() {
    return Type.CRASH;
  }

  public String getHash() {
    return stackTraceHash;
  }

  public boolean isValid() {
    return !(TextUtils.isEmpty(event) || TextUtils.isEmpty(created) || TextUtils.isEmpty(stackTraceHash));
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    // no-op
  }
}