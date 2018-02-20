package com.mapbox.android.telemetry.NavigationUtilities;

import android.content.Context;

interface AudioTypeResolver {
  void nextChain(AudioTypeResolver chain);

  String obtainAudioType(Context context);
}