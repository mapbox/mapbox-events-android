package com.mapbox.android.telemetry;

import android.content.Context;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TelemetryUtilsTest {

  @Test
  public void checkNetworkTypeUnknown() {
    Context mockedContext = mock(Context.class);
    when(mockedContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null);

    String networkType = TelemetryUtils.obtainCellularNetworkType(mockedContext);

    Assert.assertEquals("Unknown", networkType);
  }

  @Test
  public void checkApplicationStateNoState() {
    Context mockedContext = mock(Context.class);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(null);

    String applicationState = TelemetryUtils.obtainApplicationState(mockedContext);

    Assert.assertEquals("", applicationState);
  }
}
