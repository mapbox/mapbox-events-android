package com.mapbox.android.telemetry.errors;


import android.support.test.InstrumentationRegistry;

import org.junit.Test;

public class ErrorReporterJobIntentServiceInstrumentationTest {

  @Test
  public void enqueueWork() {
    ErrorReporterJobIntentService.enqueueWork(InstrumentationRegistry.getInstrumentation().getTargetContext());
    // TODO: verify work is executed
  }
}