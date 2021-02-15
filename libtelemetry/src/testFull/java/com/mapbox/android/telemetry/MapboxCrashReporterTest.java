package com.mapbox.android.telemetry;

import com.mapbox.android.core.crashreporter.CrashReport;
import com.mapbox.android.core.crashreporter.CrashReportFactory;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapboxCrashReporterTest {

  @Test
  public void reportNonMapboxException() {
    MapboxTelemetry telemetryMock = mock(MapboxTelemetry.class);
    CrashReportFactory reportFactoryMock = mock(CrashReportFactory.class);
    MapboxCrashReporter reporter = new MapboxCrashReporter(telemetryMock, reportFactoryMock);

    Throwable error = new Exception();
    when(reportFactoryMock.createReportForNonFatal(error)).thenReturn(null);

    boolean isReported = reporter.reportError(error);

    verify(telemetryMock, never()).push(any(Event.class));
    Assert.assertFalse(isReported);
  }

  @Test
  public void reportMapboxException() {
    MapboxTelemetry telemetryMock = mock(MapboxTelemetry.class);
    CrashReportFactory reportFactoryMock = mock(CrashReportFactory.class);
    MapboxCrashReporter reporter = spy(new MapboxCrashReporter(telemetryMock, reportFactoryMock));

    Throwable error = new Exception();
    CrashReport reportMock = mock(CrashReport.class);
    CrashEvent crashEvent = new CrashEvent(null, null);
    when(reportFactoryMock.createReportForNonFatal(error)).thenReturn(reportMock);
    doReturn(crashEvent).when(reporter).parseReportAsEvent(reportMock);
    when(telemetryMock.push(crashEvent)).thenReturn(true);

    boolean isReported = reporter.reportError(error);

    verify(telemetryMock, times(1)).push(crashEvent);
    Assert.assertTrue(isReported);
  }
}
