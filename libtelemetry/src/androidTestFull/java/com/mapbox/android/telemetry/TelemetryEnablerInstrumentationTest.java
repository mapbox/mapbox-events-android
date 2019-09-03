package com.mapbox.android.telemetry;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TelemetryEnablerInstrumentationTest {

  private static String LOG_TAG = "TelemetryEnablerInstrumentationTest";
  private Context context;

  @Test(expected = IllegalStateException.class)
  public void checksNonNullContextRequired() {

    new TelemetryEnabler(true, null);
  }

  @Before
  public void setUp() {

    context = InstrumentationRegistry.getTargetContext();
  }

  @Test
  public void disableTelemetryEventFromPreferences() {

    TelemetryEnabler enabler = new TelemetryEnabler(true, context);

    TelemetryEnabler.State currentState = enabler.obtainTelemetryState();

    assertEquals(TelemetryEnabler.State.ENABLED, currentState);


    enabler.updateTelemetryState(TelemetryEnabler.State.DISABLED);

    // Give it some time to listen and update the state.

    try {
      Thread.sleep(1000);
    } catch (InterruptedException exception) {
      Log.e(LOG_TAG, exception.toString());
    }

    TelemetryEnabler.State updatedState = enabler.obtainTelemetryState();


    assertEquals(TelemetryEnabler.State.DISABLED, updatedState);

  }

  @Test
  public void enableTelemetryEventFromPreferences() {

    TelemetryEnabler enabler = new TelemetryEnabler(true, context);

    TelemetryEnabler.State currentState = enabler.updateTelemetryState(TelemetryEnabler.State.DISABLED);

    // Give it some time to listen and update the state.

    try {
      Thread.sleep(1000);
    } catch (InterruptedException exception) {
      Log.e(LOG_TAG, exception.toString());
    }
    assertEquals(TelemetryEnabler.State.DISABLED, currentState);

    enabler.updateTelemetryState(TelemetryEnabler.State.ENABLED);

    // Give it some time to listen and update the state.

    try {
      Thread.sleep(1000);
    } catch (InterruptedException exception) {
      Log.e(LOG_TAG, exception.toString());
    }

    TelemetryEnabler.State updatedState = enabler.obtainTelemetryState();

    assertEquals(TelemetryEnabler.State.ENABLED, updatedState);

  }

  @Test
  public void disableTelemetryEventFromNonPreferences() {

    TelemetryEnabler enabler = new TelemetryEnabler(false, context);

    enabler.updateTelemetryState(TelemetryEnabler.State.DISABLED);

    TelemetryEnabler.State updatedState = enabler.obtainTelemetryState();

    assertEquals(TelemetryEnabler.State.DISABLED, updatedState);

  }

  @Test
  public void enableTelemetryEventFromNonPreferences() {

    TelemetryEnabler enabler = new TelemetryEnabler(false, context);

    enabler.updateTelemetryState(TelemetryEnabler.State.ENABLED);

    TelemetryEnabler.State updatedState = enabler.obtainTelemetryState();

    assertEquals(TelemetryEnabler.State.ENABLED, updatedState);

  }


}
