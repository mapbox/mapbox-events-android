package com.mapbox.android.telemetry;

import android.content.Context;
import android.media.AudioManager;

class HeadphonesAudioType implements AudioTypeResolver {
  private static final String HEADPHONES = "headphones";
  private AudioTypeResolver chain;

  @Override
  public void nextChain(AudioTypeResolver chain) {
    this.chain = chain;
  }

  @Override
  public String obtainAudioType(Context context) {
    AudioManager audioManager = (AudioManager) MapboxTelemetry.applicationContext
      .getSystemService(Context.AUDIO_SERVICE);

    if (audioManager.isWiredHeadsetOn()) {
      return HEADPHONES;
    } else {
      return chain.obtainAudioType(context);
    }
  }
}