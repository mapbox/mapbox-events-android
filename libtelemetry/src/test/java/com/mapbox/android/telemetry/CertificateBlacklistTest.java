package com.mapbox.android.telemetry;

import android.content.Context;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CertificateBlacklistTest extends MockWebServerTest {

  @Test
  public void checkDaySinceLastUpdate() throws Exception {
    Context mockContext = mock(Context.class);
    CertificateBlacklist certificateBlacklist = new CertificateBlacklist(mockContext, "test", "test");

    assertTrue(certificateBlacklist.daySinceLastUpdate());
  }
}
