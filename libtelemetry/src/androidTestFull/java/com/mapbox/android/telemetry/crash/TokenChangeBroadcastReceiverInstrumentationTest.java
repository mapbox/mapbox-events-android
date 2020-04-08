package com.mapbox.android.telemetry.crash;

import android.content.Intent;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.mapbox.android.telemetry.MapboxTelemetryConstants;
import org.junit.Before;
import org.junit.Test;

public class TokenChangeBroadcastReceiverInstrumentationTest {
  @Before
  public void setUp() {
    TokenChangeBroadcastReceiver.register(InstrumentationRegistry.getInstrumentation().getTargetContext());
  }

  @Test
  public void ensureReceiverIsInvoked() {
    LocalBroadcastManager.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext())
      .sendBroadcastSync(new Intent(MapboxTelemetryConstants.ACTION_TOKEN_CHANGED));
    // TODO: verify broadcast received
  }
}