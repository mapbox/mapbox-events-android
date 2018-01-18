package com.mapbox.android.telemetry;

import android.content.Context;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class AppUserTurnstileTest {

  @Test(expected = IllegalStateException.class)
  public void checksMapboxTelemetryNotInitialized() throws Exception {
    MapboxTelemetry.applicationContext = null;

    boolean indifferentTelemetryEnabled = false;
    new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
  }

  @Test
  public void checksMapLoadEvent() throws Exception {
    Event anAppUserTurnstileEvent = obtainAnAppUserTurnstileEvent();

    assertTrue(anAppUserTurnstileEvent instanceof AppUserTurnstile);
  }

  @Test
  public void checksAppUserTurnstileType() throws Exception {
    Event anAppUserTurnstileEvent = obtainAnAppUserTurnstileEvent();

    assertEquals(Event.Type.TURNSTILE, anAppUserTurnstileEvent.obtainType());
  }

  private Event obtainAnAppUserTurnstileEvent() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    boolean indifferentTelemetryEnabled = false;
    return new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
  }
}