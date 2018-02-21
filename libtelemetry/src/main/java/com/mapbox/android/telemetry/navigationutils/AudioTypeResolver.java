package com.mapbox.android.telemetry.navigationutils;

import android.content.Context;

interface AudioTypeResolver {
  void nextChain(AudioTypeResolver chain);

  String obtainAudioType(Context context);
}