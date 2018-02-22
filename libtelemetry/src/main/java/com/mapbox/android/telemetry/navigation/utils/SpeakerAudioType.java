package com.mapbox.android.telemetry.navigation.utils;

import android.content.Context;
import android.media.AudioManager;

class SpeakerAudioType implements AudioTypeResolver {
  private static final String SPEAKER = "speaker";
  private AudioTypeResolver chain;

  @Override
  public void nextChain(AudioTypeResolver chain) {
    this.chain = chain;
  }

  @Override
  public String obtainAudioType(Context context) {
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    if (audioManager.isSpeakerphoneOn()) {
      return SPEAKER;
    } else {
      return chain.obtainAudioType(context);
    }
  }
}