package com.mapbox.android.telemetry;

import java.util.HashMap;
import java.util.Map;

public class VisionEventFactory {
  private static final String APPLICATION_CONTEXT_CANT_BE_NULL = "Create a MapboxTelemetry instance before calling "
    + "this method.";
  private static final String NOT_A_VISION_EVENT_TYPE = "Type must be a vision event.";

  private final Map<Event.Type, VisionBuildEvent> BUILD_EVENT_VISION = new HashMap<Event.Type, VisionBuildEvent>() {
    {
      put(Event.Type.VIS_GENERAL, new VisionBuildEvent() {
        @Override
        public Event build() {
          return buildVisionEvent();
        }
      });
    }
  };

  public VisionEventFactory() {
    if (MapboxTelemetry.applicationContext == null) {
      throw new IllegalStateException(APPLICATION_CONTEXT_CANT_BE_NULL);
    }
  }

  public Event createVisionEvent(Event.Type type) {
    checkVisionEvent(type);
    return BUILD_EVENT_VISION.get(type).build();
  }

  private VisionEvent buildVisionEvent() {
    VisionEvent visionEvent = new VisionEvent();
    return visionEvent;
  }

  private void checkVisionEvent(Event.Type type) {
    checkEventType(type);
  }

  private void checkEventType(Event.Type type) {
    if (!Event.visionEventTypes.contains(type)) {
      throw new IllegalArgumentException(NOT_A_VISION_EVENT_TYPE);
    }
  }
}
