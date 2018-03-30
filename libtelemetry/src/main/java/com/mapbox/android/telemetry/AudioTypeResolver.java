package com.mapbox.android.telemetry;

import android.content.Context;
import android.media.AudioManager;

interface AudioTypeResolver {
  void nextChain(AudioTypeResolver chain);

  String obtainAudioType(Context context, AudioManager audioManager);
}