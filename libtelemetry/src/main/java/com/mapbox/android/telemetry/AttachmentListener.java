package com.mapbox.android.telemetry;

import java.util.List;

public interface AttachmentListener {
  void onAttachmentResponse(String message, int code, List<String> fieldIds);

  void onAttachmentFailure(String message, List<String> fieldIds);
}
