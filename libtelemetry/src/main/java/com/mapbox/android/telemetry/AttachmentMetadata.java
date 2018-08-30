package com.mapbox.android.telemetry;

public class AttachmentMetadata {

  private String name;
  private String created;
  private String eventId;
  private String format;
  private String type;
  private Integer size;
  private String startTime;
  private String endTime;

  AttachmentMetadata(String name, String eventId, String format, String type) {
    this.name = name;
    this.created = TelemetryUtils.obtainCurrentDate();
    this.eventId = eventId;
    this.format = format;
    this.type = type;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getName() {
    return name;
  }

  public String getCreated() {
    return created;
  }

  public String getEventId() {
    return eventId;
  }

  public String getFormat() {
    return format;
  }

  public String getType() {
    return type;
  }

  public Integer getSize() {
    return size;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getEndTime() {
    return endTime;
  }
}
