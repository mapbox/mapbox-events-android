package com.mapbox.android.telemetry;

import android.content.Context;

import org.junit.Test;

import static com.mapbox.android.telemetry.TelemetryEnabler.State;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class ConfigurationServiceTest {

  @Test
  public void checkTelemetryStateForConfig() {
    ConfigurationService service = new ConfigurationService(mock(Context.class, RETURNS_DEEP_STUBS),
      mock(ConfigurationCallback.class));
    Configuration configuration = new Configuration(new String[]{}, null, null);
    assertTrue(service.getUpdatedTelemetryState(State.CONFIG_DISABLED, configuration) == State.ENABLED);

    configuration = new Configuration(new String[]{}, 0, null);
    assertTrue(service.getUpdatedTelemetryState(State.ENABLED, configuration) == State.OVERRIDE);

    configuration = new Configuration(new String[]{}, 1, null);
    assertTrue(service.getUpdatedTelemetryState(State.OVERRIDE, configuration) == State.CONFIG_DISABLED);

    configuration = new Configuration(new String[]{}, 2, null);
    assertTrue(service.getUpdatedTelemetryState(State.CONFIG_DISABLED, configuration) == State.CONFIG_DISABLED);

    configuration = new Configuration(new String[]{}, 2, null);
    assertTrue(service.getUpdatedTelemetryState(State.ENABLED, configuration) == State.ENABLED);
  }

  @Test
  public void shouldUpdateTelemetryState() {
    ConfigurationService service = new ConfigurationService(mock(Context.class, RETURNS_DEEP_STUBS),
      mock(ConfigurationCallback.class));
    assertTrue(service.shouldUpdateTelemetryState(State.ENABLED, State.OVERRIDE));
    assertTrue(service.shouldUpdateTelemetryState(State.ENABLED, State.CONFIG_DISABLED));
    assertFalse(service.shouldUpdateTelemetryState(State.ENABLED, State.ENABLED));

    assertTrue(service.shouldUpdateTelemetryState(State.OVERRIDE, State.ENABLED));
    assertTrue(service.shouldUpdateTelemetryState(State.OVERRIDE, State.CONFIG_DISABLED));
    assertFalse(service.shouldUpdateTelemetryState(State.OVERRIDE, State.OVERRIDE));

    assertTrue(service.shouldUpdateTelemetryState(State.CONFIG_DISABLED, State.ENABLED));
    assertTrue(service.shouldUpdateTelemetryState(State.CONFIG_DISABLED, State.OVERRIDE));
    assertFalse(service.shouldUpdateTelemetryState(State.CONFIG_DISABLED, State.CONFIG_DISABLED));

    assertFalse(service.shouldUpdateTelemetryState(State.DISABLED, State.ENABLED));
    assertFalse(service.shouldUpdateTelemetryState(State.DISABLED, State.OVERRIDE));
    assertFalse(service.shouldUpdateTelemetryState(State.DISABLED, State.CONFIG_DISABLED));
  }

}
