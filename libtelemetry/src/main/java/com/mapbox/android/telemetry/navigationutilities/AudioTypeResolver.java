package com.mapbox.android.telemetry.navigationutilities;

import android.content.Context;

interface AudioTypeResolver {
  void nextChain(AudioTypeResolver chain);

  String obtainAudioType(Context context);
}