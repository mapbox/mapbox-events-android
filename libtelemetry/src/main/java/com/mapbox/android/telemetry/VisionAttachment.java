package com.mapbox.android.telemetry;

import okhttp3.MediaType;

public class VisionAttachment {
  private AttachmentMetadata attachmentMetadata;
  private String filePath;
  private MediaType mediaType;

  VisionAttachment(AttachmentMetadata attachmentMetadata, String filePath, MediaType mediaType) {
    this.attachmentMetadata = attachmentMetadata;
    this.filePath = filePath;
    this.mediaType = mediaType;
  }

  public AttachmentMetadata getAttachmentMetadata() {
    return attachmentMetadata;
  }

  public AttachmentFilePath getFilePath() {
    return new AttachmentFilePath(filePath, mediaType);
  }
}
