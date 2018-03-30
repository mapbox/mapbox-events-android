package com.mapbox.android.telemetry;

import android.content.Context;
import android.media.AudioManager;

class UnknownAudioType implements AudioTypeResolver {
  private static final String UNKNOWN = "unknown";

  @Override
  public void nextChain(AudioTypeResolver chain) {
  }

  @Override
  public String obtainAudioType(Context context, AudioManager audioManager) {
    return UNKNOWN;
  }
}
