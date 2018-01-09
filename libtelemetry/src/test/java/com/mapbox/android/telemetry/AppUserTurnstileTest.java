package com.mapbox.android.telemetry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppUserTurnstileTest {

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
    boolean indifferentTelemetryEnabled = false;
    return new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
  }
}