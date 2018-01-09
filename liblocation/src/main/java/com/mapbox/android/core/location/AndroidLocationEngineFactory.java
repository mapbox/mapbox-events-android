package com.mapbox.android.core.location;


import android.content.Context;

class AndroidLocationEngineFactory implements LocationEngineSupplier {

  @Override
  public LocationEngine supply(Context context) {
    return AndroidLocationEngine.getLocationEngine(context);
  }

  @Override
  public boolean hasDependencyOnClasspath() {
    return true;
  }
}
