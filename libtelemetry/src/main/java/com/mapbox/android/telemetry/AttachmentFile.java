package com.mapbox.android.telemetry;

public class AttachmentFile {
  private String type;
  private String filePath;

  AttachmentFile(String type, String filePath) {
    this.type = type;
    this.filePath = filePath;
  }

  public String getType() {
    return type;
  }

  public String getFilePath() {
    return filePath;
  }
}
