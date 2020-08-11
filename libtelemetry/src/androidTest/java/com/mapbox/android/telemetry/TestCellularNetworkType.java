package com.mapbox.android.telemetry;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

public class TestCellularNetworkType {
  @Test
  public void testCellularNetworkType() {
    Context context = InstrumentationRegistry.getInstrumentation().getContext();
    String cellularNetworkType = TelemetryUtils.obtainCellularNetworkType(context);
    Assert.assertFalse(cellularNetworkType.isEmpty());
  }
}
