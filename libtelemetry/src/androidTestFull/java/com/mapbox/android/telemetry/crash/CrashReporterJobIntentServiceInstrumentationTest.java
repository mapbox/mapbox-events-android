package com.mapbox.android.telemetry.crash;

import android.support.test.InstrumentationRegistry;
import org.junit.Test;

public class CrashReporterJobIntentServiceInstrumentationTest {

  @Test
  public void enqueueWork() {
    CrashReporterJobIntentService.enqueueWork(InstrumentationRegistry.getTargetContext());
    // TODO: verify work is executed
  }
}