package com.mapbox.android.telemetry;


import com.google.gson.annotations.SerializedName;

class TelemetryResponse {
  @SerializedName("message")
  private final String message;

  TelemetryResponse(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object response) {
    if (this == response) {
      return true;
    }
    if (response == null || getClass() != response.getClass()) {
      return false;
    }

    TelemetryResponse otherResponse = (TelemetryResponse) response;

    return message != null ? message.equals(otherResponse.message) : otherResponse.message == null;
  }

  @Override
  public int hashCode() {
    return message != null ? message.hashCode() : 0;
  }
}
