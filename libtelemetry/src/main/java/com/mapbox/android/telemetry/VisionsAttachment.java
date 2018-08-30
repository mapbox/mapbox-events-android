package com.mapbox.android.telemetry;

public class VisionsAttachment {
  private AttachmentMetadata attachmentMetadata;
  private String filePath;

  VisionsAttachment(AttachmentMetadata attachmentMetadata, String filePath) {
    this.attachmentMetadata = attachmentMetadata;
    this.filePath = filePath;
  }

  public AttachmentMetadata getAttachmentMetadata() {
    return attachmentMetadata;
  }

  public String getFilePath() {
    return filePath;
  }
}
