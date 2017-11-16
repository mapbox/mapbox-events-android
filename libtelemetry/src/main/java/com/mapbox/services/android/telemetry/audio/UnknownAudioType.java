package com.mapbox.services.android.telemetry.audio;

import android.content.Context;

public class UnknownAudioType implements AudioTypeResolver {
  private static final String UNKNOWN = "unknown";

  @Override
  public void nextChain(AudioTypeResolver chain) {
  }

  @Override
  public String obtainAudioType(Context context) {
    return UNKNOWN;
  }
}
