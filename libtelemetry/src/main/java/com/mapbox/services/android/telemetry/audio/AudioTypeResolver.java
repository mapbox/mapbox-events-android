package com.mapbox.services.android.telemetry.audio;

import android.content.Context;

public interface AudioTypeResolver {
  void nextChain(AudioTypeResolver chain);

  String obtainAudioType(Context context);
}