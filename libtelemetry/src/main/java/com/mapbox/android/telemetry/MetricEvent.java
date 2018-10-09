package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

class MetricEvent extends Event implements Parcelable {
  private static final String TELEMETRY_METRIC = "telemetryMetrics";

  @SerializedName("event")
  private final String event;
  @SerializedName("created")
  private final String created;
  @SerializedName("dateUTC")
  private String dateUTC;
  @SerializedName("requests")
  private Integer requests;
  @SerializedName("failedRequests")
  private String failedRequests;
  @SerializedName("totalDataTransfer")
  private Integer totalDataTransfer;
  @SerializedName("cellDataTransfer")
  private Integer cellDataTransfer;
  @SerializedName("wifiDataTransfer")
  private Integer wifiDataTransfer;
  @SerializedName("appWakeups")
  private Integer appWakeups;
  @SerializedName("eventCountPerType")
  private String eventCountPerType;
  @SerializedName("eventCountFailed")
  private Integer eventCountFailed;
  @SerializedName("eventCountTotal")
  private Integer eventCountTotal;
  @SerializedName("eventCountMax")
  private Integer eventCountMax;
  @SerializedName("deviceLat")
  private Double deviceLat;
  @SerializedName("deviceLon")
  private Double deviceLon;
  @SerializedName("deviceTimeDrift")
  private Integer deviceTimeDrift;
  @SerializedName("configResponse")
  private String configResponse;

  public MetricEvent() {
    this.event = TELEMETRY_METRIC;
    this.created = TelemetryUtils.obtainCurrentDate();
  }

  public void setDateUTC(String dateUTC) {
    this.dateUTC = dateUTC;
  }

  public void setRequests(Integer requests) {
    this.requests = requests;
  }

  public void setFailedRequests(String failedRequests) {
    this.failedRequests = failedRequests;
  }

  public void setTotalDataTransfer(Integer totalDataTransfer) {
    this.totalDataTransfer = totalDataTransfer;
  }

  public void setCellDataTransfer(Integer cellDataTransfer) {
    this.cellDataTransfer = cellDataTransfer;
  }

  public void setWifiDataTransfer(Integer wifiDataTransfer) {
    this.wifiDataTransfer = wifiDataTransfer;
  }

  public void setAppWakeups(Integer appWakeups) {
    this.appWakeups = appWakeups;
  }

  public void setEventCountPerType(String eventCountPerType) {
    this.eventCountPerType = eventCountPerType;
  }

  public void setEventCountFailed(Integer eventCountFailed) {
    this.eventCountFailed = eventCountFailed;
  }

  public void setEventCountTotal(Integer eventCountTotal) {
    this.eventCountTotal = eventCountTotal;
  }

  public void setEventCountMax(Integer eventCountMax) {
    this.eventCountMax = eventCountMax;
  }

  public void setDeviceLat(Double deviceLat) {
    this.deviceLat = deviceLat;
  }

  public void setDeviceLon(Double deviceLon) {
    this.deviceLon = deviceLon;
  }

  public void setConfigResponse(String configResponse) {
    this.configResponse = configResponse;
  }

  @Override
  Type obtainType() {
    return Type.TELEMETRY_METRIC;
  }

  MetricEvent(Parcel in) {
    event = in.readString();
    created = in.readString();
    dateUTC = in.readString();
    if (in.readByte() == 0) {
      requests = null;
    } else {
      requests = in.readInt();
    }
    failedRequests = in.readString();
    if (in.readByte() == 0) {
      totalDataTransfer = null;
    } else {
      totalDataTransfer = in.readInt();
    }
    if (in.readByte() == 0) {
      cellDataTransfer = null;
    } else {
      cellDataTransfer = in.readInt();
    }
    if (in.readByte() == 0) {
      wifiDataTransfer = null;
    } else {
      wifiDataTransfer = in.readInt();
    }
    if (in.readByte() == 0) {
      appWakeups = null;
    } else {
      appWakeups = in.readInt();
    }
    eventCountPerType = in.readString();
    if (in.readByte() == 0) {
      eventCountFailed = null;
    } else {
      eventCountFailed = in.readInt();
    }
    if (in.readByte() == 0) {
      eventCountTotal = null;
    } else {
      eventCountTotal = in.readInt();
    }
    if (in.readByte() == 0) {
      eventCountMax = null;
    } else {
      eventCountMax = in.readInt();
    }
    if (in.readByte() == 0) {
      deviceLat = null;
    } else {
      deviceLat = in.readDouble();
    }
    if (in.readByte() == 0) {
      deviceLon = null;
    } else {
      deviceLon = in.readDouble();
    }
    if (in.readByte() == 0) {
      deviceTimeDrift = null;
    } else {
      deviceTimeDrift = in.readInt();
    }
    configResponse = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(event);
    dest.writeString(created);
    dest.writeString(dateUTC);
    if (requests == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeInt(requests);
    }
    dest.writeString(failedRequests);
    if (totalDataTransfer == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeInt(totalDataTransfer);
    }
    if (cellDataTransfer == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeInt(cellDataTransfer);
    }
    if (wifiDataTransfer == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeInt(wifiDataTransfer);
    }
    if (appWakeups == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeInt(appWakeups);
    }
    dest.writeString(eventCountPerType);
    if (eventCountFailed == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeInt(eventCountFailed);
    }
    if (eventCountTotal == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeInt(eventCountTotal);
    }
    if (eventCountMax == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeInt(eventCountMax);
    }
    if (deviceLat == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeDouble(deviceLat);
    }
    if (deviceLon == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeDouble(deviceLon);
    }
    if (deviceTimeDrift == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeInt(deviceTimeDrift);
    }
    dest.writeString(configResponse);
  }

  public static final Creator<MetricEvent> CREATOR = new Creator<MetricEvent>() {
    @Override
    public MetricEvent createFromParcel(Parcel in) {
      return new MetricEvent(in);
    }

    @Override
    public MetricEvent[] newArray(int size) {
      return new MetricEvent[size];
    }
  };
}
