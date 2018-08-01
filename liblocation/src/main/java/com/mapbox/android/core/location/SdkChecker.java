package com.mapbox.android.core.location;

import android.os.Build;

public class SdkChecker {

  boolean isOreoOrAbove() {
    return Build.VERSION.SDK_INT >= 26;
  }
}
