package com.mapbox.android.telemetry;

import okhttp3.MediaType;

class AttachmentFilePath {
  String filePath;
  MediaType type;

  AttachmentFilePath(String filePath, MediaType type) {
    this.filePath = filePath;
    this.type = type;
  }
}
