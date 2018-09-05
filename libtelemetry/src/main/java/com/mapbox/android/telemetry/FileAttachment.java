package com.mapbox.android.telemetry;

import okhttp3.MediaType;

public class FileAttachment {
  private AttachmentMetadata attachmentMetadata;
  private String filePath;
  private MediaType mediaType;

  FileAttachment(AttachmentMetadata attachmentMetadata, String filePath, MediaType mediaType) {
    this.attachmentMetadata = attachmentMetadata;
    this.filePath = filePath;
    this.mediaType = mediaType;
  }

  public AttachmentMetadata getAttachmentMetadata() {
    return attachmentMetadata;
  }

  public FileData getFileData() {
    return new FileData(filePath, mediaType);
  }
}
