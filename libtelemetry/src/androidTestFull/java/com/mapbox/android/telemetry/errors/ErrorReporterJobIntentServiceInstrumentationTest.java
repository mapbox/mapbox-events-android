package com.mapbox.android.telemetry.errors;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

public class ErrorReporterJobIntentServiceInstrumentationTest {

  @Test
  public void enqueueWork() {
    ErrorReporterJobIntentService.enqueueWork(InstrumentationRegistry.getInstrumentation().getTargetContext());
    // TODO: verify work is executed
  }
}