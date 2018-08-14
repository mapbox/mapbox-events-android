package com.mapbox.android.telemetry;

import java.util.HashMap;
import java.util.Map;

public class VisionEventFactory {
  private static final String APPLICATION_CONTEXT_CANT_BE_NULL = "Create a MapboxTelemetry instance before calling "
    + "this method.";
  private static final String NOT_A_VISION_EVENT_TYPE = "Type must be a vision event.";
  private static final String NAME_ILLEGAL_NULL = "Name cannot be null.";
  private static final String CONTENTS_ILLEGAL_NULL = "Contents cannot be null.";

  private final Map<Event.Type, VisionBuildEvent> BUILD_EVENT_VISION = new HashMap<Event.Type, VisionBuildEvent>() {
    {
      put(Event.Type.VIS_GENERAL, new VisionBuildEvent() {
        @Override
        public Event build(String name, HashMap<String, Object> contents) {
          return buildVisionEvent(name, contents);
        }
      });
    }
  };

  public VisionEventFactory() {
    if (MapboxTelemetry.applicationContext == null) {
      throw new IllegalStateException(APPLICATION_CONTEXT_CANT_BE_NULL);
    }
  }

  public Event createVisionEvent(Event.Type type, String name,  HashMap<String, Object> contents) {
    checkVisionEvent(type, name, contents);
    return BUILD_EVENT_VISION.get(type).build(name, contents);
  }

  private VisionEvent buildVisionEvent(String name, HashMap<String, Object> contents) {
    VisionEvent visionEvent = new VisionEvent(name, contents);
    return visionEvent;
  }

  private void checkVisionEvent(Event.Type type, String name, HashMap<String, Object> contents) {
    checkEventType(type);
    nameIsNotNull(name);
    contentsAreNotNull(contents);
  }

  private void checkEventType(Event.Type type) {
    if (!Event.visionEventTypes.contains(type)) {
      throw new IllegalArgumentException(NOT_A_VISION_EVENT_TYPE);
    }
  }

  private void nameIsNotNull(String name) {
    if (name == null) {
      throw new IllegalArgumentException(NAME_ILLEGAL_NULL);
    }
  }

  private void contentsAreNotNull(HashMap<String, Object> contents) {
    if (contents == null) {
      throw new IllegalArgumentException(CONTENTS_ILLEGAL_NULL);
    }
  }
}
