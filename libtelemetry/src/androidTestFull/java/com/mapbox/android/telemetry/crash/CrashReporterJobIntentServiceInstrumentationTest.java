package com.mapbox.android.telemetry.crash;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

public class CrashReporterJobIntentServiceInstrumentationTest {

  @Test
  public void enqueueWork() {
    CrashReporterJobIntentService.enqueueWork(InstrumentationRegistry.getInstrumentation().getTargetContext());
    // TODO: verify work is executed
  }
}