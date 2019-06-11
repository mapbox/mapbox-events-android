package com.mapbox.android.telemetry.metrics.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.mapbox.android.telemetry.metrics.TelemetryMetrics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static android.net.ConnectivityManager.TYPE_WIFI;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@RunWith(MockitoJUnitRunner.class)
public class NetworkUsageMetricsCollectorTest {

  @Mock
  private TelemetryMetrics metrics;

  private NetworkUsageMetricsCollector networkUsageMetricsCollector;

  @Before
  public void setUp() {
    networkUsageMetricsCollector = new NetworkUsageMetricsCollector(getMockedContext(TYPE_WIFI), metrics);
  }

  @Test
  public void addRxBytes() {
    networkUsageMetricsCollector.addRxBytes(30);
    verify(metrics).addRxBytesForType(anyInt(), anyLong());
  }

  @Test
  public void addTxBytes() {
    networkUsageMetricsCollector.addTxBytes(30);
    verify(metrics).addTxBytesForType(anyInt(), anyLong());
  }

  private static Context getMockedContext(int networkType) {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ConnectivityManager mockedCm = mock(ConnectivityManager.class, RETURNS_DEEP_STUBS);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class, RETURNS_DEEP_STUBS);
    when(mockedNetworkInfo.getType()).thenReturn(networkType);
    when(mockedCm.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedContext.getSystemService(anyString())).thenReturn(mockedCm);
    return mockedContext;
  }
}