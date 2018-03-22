package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Test;

import static com.mapbox.android.telemetry.TelemetryEnabler.MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE;
import static com.mapbox.android.telemetry.TelemetryUtils.MAPBOX_SHARED_PREFERENCES;
import static com.mapbox.android.telemetry.TelemetryUtils.MAPBOX_SHARED_PREFERENCE_KEY_VENDOR_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppUserTurnstileTest {

  @Test(expected = IllegalStateException.class)
  public void checksMapboxTelemetryNotInitialized() throws Exception {
    MapboxTelemetry.applicationContext = null;

    new AppUserTurnstile("anySdkIdentifier", "anySdkVersion", false);
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
    Context mockedContext = mock(Context.class);
    SharedPreferences mockedSharedPreferences = mock(SharedPreferences.class);
    when(mockedContext.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE))
      .thenReturn(mockedSharedPreferences);
    when(mockedSharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE,
      TelemetryEnabler.State.DISABLED.name())).thenReturn(TelemetryEnabler.State.DISABLED.name());
    when(mockedSharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_VENDOR_ID, "")).thenReturn("");
    SharedPreferences.Editor mockedEditor = mock(SharedPreferences.Editor.class);
    when(mockedSharedPreferences.edit()).thenReturn(mockedEditor);
    MapboxTelemetry.applicationContext = mockedContext;
    return new AppUserTurnstile("anySdkIdentifier", "anySdkVersion", false);
  }
}