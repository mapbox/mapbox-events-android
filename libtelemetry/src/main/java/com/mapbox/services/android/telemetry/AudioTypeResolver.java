package com.mapbox.services.android.telemetry;

import android.content.Context;

interface AudioTypeResolver {
  void nextChain(AudioTypeResolver chain);

  String obtainAudioType(Context context);
}