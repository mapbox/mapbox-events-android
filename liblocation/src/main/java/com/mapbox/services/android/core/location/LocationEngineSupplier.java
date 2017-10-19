package com.mapbox.services.android.core.location;


import android.content.Context;

interface LocationEngineSupplier {

  LocationEngine supply(Context context);

  boolean hasDependencyOnClasspath();
}
