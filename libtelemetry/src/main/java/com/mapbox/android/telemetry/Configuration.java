package com.mapbox.android.telemetry;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class Configuration {

  @SerializedName("crl")
  private String[] certificateBlacklists;

  @SerializedName("tto")
  private Integer type;

  @SerializedName("tag")
  private String eventTag;

  public Configuration(String[] certificateBlacklists, Integer type, String eventTag) {
    this.certificateBlacklists = certificateBlacklists;
    this.type = type;
    this.eventTag = eventTag;
  }

  public String[] getCertificateBlacklists() {
    return certificateBlacklists;
  }

  public Integer getType() {
    return type;
  }

  public String getEventTag() {
    return eventTag;
  }

  @Override
  public String toString() {
    return "Configuration{"
      + "certificateBlacklists=" + Arrays.toString(certificateBlacklists)
      + ", type=" + type
      + ", eventTag='" + eventTag
      + '}';
  }
}
