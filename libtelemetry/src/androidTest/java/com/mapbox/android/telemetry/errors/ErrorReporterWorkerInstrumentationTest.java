package com.mapbox.android.telemetry.errors;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import org.junit.Test;

import com.mapbox.android.telemetry.MapboxTelemetryConstants;

public class ErrorReporterWorkerInstrumentationTest {

  @Test
  public void enqueueWork() {
    WorkManager.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext())
      .enqueueUniqueWork(
        MapboxTelemetryConstants.ACTION_TOKEN_CHANGED,
        ExistingWorkPolicy.KEEP,
        ErrorReporterWorker.createWorkRequest());
    // TODO: verify work is executed
  }
}