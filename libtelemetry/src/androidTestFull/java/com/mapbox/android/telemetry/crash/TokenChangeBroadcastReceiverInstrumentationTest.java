package com.mapbox.android.telemetry.crash;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.v4.content.LocalBroadcastManager;
import com.mapbox.android.telemetry.MapboxTelemetryConstants;
import org.junit.Before;
import org.junit.Test;

public class TokenChangeBroadcastReceiverInstrumentationTest {
  @Before
  public void setUp() {
    TokenChangeBroadcastReceiver.register(InstrumentationRegistry.getTargetContext());
  }

  @Test
  public void ensureReceiverIsInvoked() {
    LocalBroadcastManager.getInstance(InstrumentationRegistry.getTargetContext())
      .sendBroadcastSync(new Intent(MapboxTelemetryConstants.ACTION_TOKEN_CHANGED));
    // TODO: verify broadcast received
  }
}