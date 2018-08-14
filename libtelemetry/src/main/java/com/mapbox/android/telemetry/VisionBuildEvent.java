package com.mapbox.android.telemetry;

import java.util.HashMap;

public interface VisionBuildEvent {
  Event build(String name, HashMap<String, Object> contents);
}
