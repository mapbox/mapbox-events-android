package com.mapbox.android.core.location;

import android.os.Build;

public class SdkChecker {
  private static final int OREO = 26;

  static boolean isOreoOrAbove() {
    return Build.VERSION.SDK_INT >= OREO;
  }
}
