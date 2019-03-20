package com.mapbox.android.events.testapp;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;

public class MainApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      return;
    }
    LeakCanary.install(this);
  }
}
