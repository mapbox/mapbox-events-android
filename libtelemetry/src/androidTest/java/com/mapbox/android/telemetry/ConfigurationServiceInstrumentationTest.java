package com.mapbox.android.telemetry;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ConfigurationServiceInstrumentationTest {

  private ConfigurationService configurationService;

  @Before
  public void setup() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    MapboxTelemetry.applicationContext = context.getApplicationContext();
    this.configurationService = new ConfigurationService(context, mock(ConfigurationCallback.class));
  }

  @Test
  public void checkStateChanged() {
    TelemetryEnabler.updateTelemetryState(TelemetryEnabler.State.ENABLED);
    configurationService.setCurrentState(TelemetryEnabler.State.ENABLED);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(true);
    Configuration configuration = new Configuration(new String[]{}, null, null);
    assertFalse(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.ENABLED);

    configuration = new Configuration(new String[]{}, 0, null);
    assertFalse(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.OVERRIDE);

    configuration = new Configuration(new String[]{}, 1, null);
    assertTrue(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.CONFIG_DISABLED);

    configuration = new Configuration(new String[]{}, null, null);
    assertTrue(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.ENABLED);

    configuration = new Configuration(new String[]{}, 1, null);
    assertTrue(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.CONFIG_DISABLED);

    configuration = new Configuration(new String[]{}, 0, null);
    assertTrue(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.OVERRIDE);

    configuration = new Configuration(new String[]{}, null, null);
    assertFalse(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.ENABLED);
  }

  @Test
  public void checkStateNotChanged() {
    TelemetryEnabler.updateTelemetryState(TelemetryEnabler.State.DISABLED);
    configurationService.setCurrentState(TelemetryEnabler.State.DISABLED);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(true);
    Configuration configuration = new Configuration(new String[]{}, null, null);
    assertFalse(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.DISABLED);

    configuration = new Configuration(new String[]{}, 0, null);
    assertFalse(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.DISABLED);

    configuration = new Configuration(new String[]{}, 1, null);
    assertFalse(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.DISABLED);

    configuration = new Configuration(new String[]{}, null, null);
    assertFalse(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.DISABLED);

    configuration = new Configuration(new String[]{}, 1, null);
    assertFalse(configurationService.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.DISABLED);
  }

  @Test
  public void validateEventsStateAfterConfigChange() {
    TelemetryEnabler.updateTelemetryState(TelemetryEnabler.State.ENABLED);
    configurationService.setCurrentState(TelemetryEnabler.State.ENABLED);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(true);
    Configuration configuration = new Configuration(new String[]{}, null, null);
    assertFalse(configurationService.updateTelemetryState(configuration));
    assertTrue(TelemetryEnabler.isEventsEnabled(telemetryEnabler));

    configuration = new Configuration(new String[]{}, 0, null);
    assertFalse(configurationService.updateTelemetryState(configuration));
    assertTrue(TelemetryEnabler.isEventsEnabled(telemetryEnabler));

    configuration = new Configuration(new String[]{}, 1, null);
    assertTrue(configurationService.updateTelemetryState(configuration));
    assertFalse(TelemetryEnabler.isEventsEnabled(telemetryEnabler));
  }
}
