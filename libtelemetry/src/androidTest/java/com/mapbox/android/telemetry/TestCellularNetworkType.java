package com.mapbox.android.telemetry;

import android.content.Context;
import android.telephony.TelephonyManager;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

public class TestCellularNetworkType {
  @Test
  public void testCellularNetworkType() {
    Context context = InstrumentationRegistry.getInstrumentation().getContext();
    String cellularNetworkType = TelemetryUtils.obtainCellularNetworkType(context);
    Assert.assertFalse(cellularNetworkType.isEmpty());
  }

  @Test
  public void testUnknownNetworkType() {
    Context mockedContext = mock(Context.class);
    TelephonyManager mockedTelephonyManager = mock(TelephonyManager.class);
    when(mockedContext.getSystemService(any(String.class))).thenReturn(mockedTelephonyManager);
    when(mockedTelephonyManager.getDataNetworkType()).thenReturn(TelephonyManager.NETWORK_TYPE_UNKNOWN);
    Assert.assertEquals(TelemetryUtils.obtainCellularNetworkType(mockedContext), "Unknown");
    when(mockedTelephonyManager.getDataNetworkType()).thenReturn(Integer.MAX_VALUE);
    Assert.assertEquals(TelemetryUtils.obtainCellularNetworkType(mockedContext), "Unknown");
  }
}
