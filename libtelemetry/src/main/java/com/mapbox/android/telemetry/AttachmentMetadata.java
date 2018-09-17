package com.mapbox.android.telemetry;

public class AttachmentMetadata {

  private String name;
  private String created;
  private String fileId;
  private String format;
  private String type;
  private String sessionId;
  private Integer size;
  private String startTime;
  private String endTime;

  public AttachmentMetadata(String name, String fileId, String format, String type, String sessionId) {
    this.name = name;
    this.created = TelemetryUtils.obtainCurrentDate();
    this.fileId = fileId;
    this.format = format;
    this.type = type;
    this.sessionId = sessionId;
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

  public String getFileId() {
    return fileId;
  }

  public String getFormat() {
    return format;
  }

  public String getType() {
    return type;
  }

  public String getSessionId() {
    return sessionId;
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
