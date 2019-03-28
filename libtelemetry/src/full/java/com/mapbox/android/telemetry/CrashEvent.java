package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.text.TextUtils;

/**
 * This class is temporary and exists only
 * to comply with legacy telemetry interface,
 * we should open up a way to send raw json data to
 * the telemetry endpoint in the future to avoid,
 * back and forth json serialization overhead.
 */
@SuppressLint("ParcelCreator")
public class CrashEvent extends Event {
  private final String event;
  private final String created;

  private String sdkIdentifier;
  private String sdkVersion;
  private String osVersion;
  private String model;
  private String device;
  private String isSilent;
  private String stackTraceHash;
  private String stackTrace;
  private String threadDetails;
  private String appId;
  private String appVersion;

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