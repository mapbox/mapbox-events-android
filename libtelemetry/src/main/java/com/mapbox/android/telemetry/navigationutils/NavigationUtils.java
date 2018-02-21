package com.mapbox.android.telemetry.navigationutils;

import android.content.Context;

public class NavigationUtils {

  public static String obtainAudioType(Context context) {
    AudioTypeChain audioTypeChain = new AudioTypeChain();
    AudioTypeResolver setupChain = audioTypeChain.setup();

    return setupChain.obtainAudioType(context);
  }
}
