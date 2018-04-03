package com.mapbox.android.telemetry;

import android.content.Context;

class UnknownAudioType implements AudioTypeResolver {
  private static final String UNKNOWN = "unknown";

  @Override
  public void nextChain(AudioTypeResolver chain) {
  }

  @Override
  public String obtainAudioType(Context context) {
    return UNKNOWN;
  }
}
