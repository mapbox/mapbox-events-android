package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.junit.Test;

import okhttp3.OkHttpClient;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CertificateBlacklistTest extends MockWebServerTest {

  @Test
  public void checkDaySinceLastUpdate() throws Exception {
    Context mockedContext = mock(Context.class);
    CertificateBlacklist certificateBlacklist = new CertificateBlacklist(mockedContext, "anAccessToken", "AnUserAgent");

    assertTrue(certificateBlacklist.daySinceLastUpdate());
  }

  @Test
  public void checkRequestContainsUserAgentHeader() throws Exception {
    TelemetryClientSettings settings = provideDefaultTelemetryClientSettings();
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    OkHttpClient client = settings.getClient(mockedBlacklist);
    Context mockedContext = obtainBlacklistContext();

    CertificateBlacklist certificateBlacklist = new CertificateBlacklist(mockedContext, "anAccessToken",
      "theUserAgent", client);
    certificateBlacklist.updateBlacklist(obtainBaseEndpointUrl());

    assertRequestContainsHeader("User-Agent", "theUserAgent");
  }

  private Context obtainBlacklistContext() throws PackageManager.NameNotFoundException {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    String anyAppInfoHostname = "any.app.info.hostname";
    String theAppInfoAccessToken = "theAppInfoAccessToken";
    Bundle mockedBundle = obtainStagingBundle(anyAppInfoHostname, theAppInfoAccessToken);
    ApplicationInfo mockedApplicationInfo = mock(ApplicationInfo.class);
    mockedApplicationInfo.metaData = mockedBundle;
    String packageName = "com.foo.test";
    when(mockedContext.getPackageManager()
      .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    ).thenReturn(mockedApplicationInfo);
    when(mockedContext.getPackageName()
    ).thenReturn(packageName);

    return mockedContext;
  }

  private Bundle obtainStagingBundle(String hostname, String accessToken) {
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedBundle.getString(eq("com.mapbox.TestEventsServer"))
    ).thenReturn(hostname);
    when(mockedBundle.getString(eq("com.mapbox.TestEventsAccessToken"))
    ).thenReturn(accessToken);
    return mockedBundle;
  }
}
