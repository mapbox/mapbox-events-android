package com.mapbox.android.telemetry;

import okhttp3.MediaType;

class FileData {
  private final String filePath;
  private final MediaType type;

  FileData(String filePath, MediaType type) {
    this.filePath = filePath;
    this.type = type;
  }

  public String getFilePath() {
    return filePath;
  }

  public MediaType getType() {
    return type;
  }
}
