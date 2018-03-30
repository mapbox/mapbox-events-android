package com.mapbox.android.telemetry.navigation.utils;

import android.content.Context;

public class NavigationUtils {

  static String obtainAudioType(Context context) {
    AudioTypeChain audioTypeChain = new AudioTypeChain();
    AudioTypeResolver setupChain = audioTypeChain.setup();

    return setupChain.obtainAudioType(context);
  }
}
