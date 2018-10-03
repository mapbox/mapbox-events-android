package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TelemetryClientFactoryTest {

  @Test
  public void checksChinaEnvironment() throws Exception {
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";
    Logger mockedLogger = mock(Logger.class);
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(anyAccessToken, anyUserAgent,
      mockedLogger, mockedBlacklist);
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedBundle
      .getBoolean(eq("com.mapbox.CnEventsServer")))
      .thenReturn(true);
    Context mockedContext = obtainMockedContext(mockedBundle);

    TelemetryClient actual = telemetryClientFactory.obtainTelemetryClient(mockedContext);

    assertEquals(Environment.CHINA, actual.obtainSetting().getEnvironment());
  }

  @Test
  public void checksStagingEnvironment() throws Exception {
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";
    Logger mockedLogger = mock(Logger.class);
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(anyAccessToken, anyUserAgent,
      mockedLogger, mockedBlacklist);
    String anyAppInfoHostname = "any.app.info.hostname";
    String anyAppInfoAccessToken = "anyAppInfoAccessToken";
    Bundle mockedBundle = obtainStagingBundle(anyAppInfoHostname, anyAppInfoAccessToken);
    Context mockedContext = obtainMockedContext(mockedBundle);

    TelemetryClient actual = telemetryClientFactory.obtainTelemetryClient(mockedContext);

    assertEquals(Environment.STAGING, actual.obtainSetting().getEnvironment());
  }

  @Test
  public void checksStagingAppInfoHostname() throws Exception {
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";
    Logger mockedLogger = mock(Logger.class);
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(anyAccessToken, anyUserAgent,
      mockedLogger, mockedBlacklist);
    String theAppInfoHostname = "the.app.info.hostname";
    String anyAppInfoAccessToken = "anyAppInfoAccessToken";
    Bundle mockedBundle = obtainStagingBundle(theAppInfoHostname, anyAppInfoAccessToken);
    Context mockedContext = obtainMockedContext(mockedBundle);

    TelemetryClient actual = telemetryClientFactory.obtainTelemetryClient(mockedContext);

    assertEquals(TelemetryClientSettings.configureUrlHostname(theAppInfoHostname), actual.obtainSetting().getBaseUrl());
  }

  @Test
  public void checksStagingAppInfoAccessToken() throws Exception {
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";
    Logger mockedLogger = mock(Logger.class);
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(anyAccessToken, anyUserAgent,
      mockedLogger, mockedBlacklist);
    String anyAppInfoHostname = "any.app.info.hostname";
    String theAppInfoAccessToken = "theAppInfoAccessToken";
    Bundle mockedBundle = obtainStagingBundle(anyAppInfoHostname, theAppInfoAccessToken);
    Context mockedContext = obtainMockedContext(mockedBundle);

    TelemetryClient actual = telemetryClientFactory.obtainTelemetryClient(mockedContext);

    assertEquals("theAppInfoAccessToken", actual.obtainAccessToken());
  }

  @Test
  public void checksCom() throws Exception {
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";
    Logger mockedLogger = mock(Logger.class);
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(anyAccessToken, anyUserAgent,
      mockedLogger, mockedBlacklist);
    Bundle mockedBundle = mock(Bundle.class);
    Context mockedContext = obtainMockedContext(mockedBundle);

    TelemetryClient actual = telemetryClientFactory.obtainTelemetryClient(mockedContext);

    assertEquals(Environment.COM, actual.obtainSetting().getEnvironment());
  }

  @Test
  public void checksDefaultComWhenAppInformationIsNull() throws Exception {
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";
    Logger mockedLogger = mock(Logger.class);
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(anyAccessToken, anyUserAgent,
      mockedLogger, mockedBlacklist);
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    setup(mockedContext, null);

    TelemetryClient actual = telemetryClientFactory.obtainTelemetryClient(mockedContext);

    assertEquals(Environment.COM, actual.obtainSetting().getEnvironment());
  }

  @Test
  public void checksDefaultComWhenAppInformationMetaDataIsNull() throws Exception {
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";
    Logger mockedLogger = mock(Logger.class);
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(anyAccessToken, anyUserAgent,
      mockedLogger, mockedBlacklist);
    Context mockedContext = obtainMockedContext(null);

    TelemetryClient actual = telemetryClientFactory.obtainTelemetryClient(mockedContext);

    assertEquals(Environment.COM, actual.obtainSetting().getEnvironment());
  }

  @Test
  public void checksDefaultComWhenErrorRetrievingAppInformation() throws Exception {
    String anyAccessToken = "anyAccessToken";
    String anyUserAgent = "anyUserAgent";
    Logger mockedLogger = mock(Logger.class);
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(anyAccessToken, anyUserAgent,
      mockedLogger, mockedBlacklist);
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    setupThrowExceptionRetrievingAppInfo(mockedContext, mockedLogger);

    TelemetryClient actual = telemetryClientFactory.obtainTelemetryClient(mockedContext);

    verify(mockedLogger, times(1))
      .error(eq("TelemetryClientFactory"), contains("Failed when retrieving app meta-data: "));
    assertEquals(Environment.COM, actual.obtainSetting().getEnvironment());
  }

  private Context obtainMockedContext(Bundle bundle) throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ApplicationInfo mockedApplicationInfo = mock(ApplicationInfo.class);
    mockedApplicationInfo.metaData = bundle;
    setup(mockedContext, mockedApplicationInfo);

    return mockedContext;
  }

  private void setup(Context mockedContext, ApplicationInfo mockedApplicationInfo) throws Exception {
    String packageName = "com.foo.test";
    when(mockedContext.getPackageManager()
      .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    ).thenReturn(mockedApplicationInfo);
    when(mockedContext.getPackageName()
    ).thenReturn(packageName);
  }

  private Bundle obtainStagingBundle(String hostname, String accessToken) {
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedBundle.getString(eq("com.mapbox.TestEventsServer"))
    ).thenReturn(hostname);
    when(mockedBundle.getString(eq("com.mapbox.TestEventsAccessToken"))
    ).thenReturn(accessToken);
    return mockedBundle;
  }

  private void setupThrowExceptionRetrievingAppInfo(Context mockedContext, Logger mockedLogger) throws Exception {
    String packageName = "com.foo.test";
    when(mockedContext.getPackageManager()
      .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    ).thenThrow(new PackageManager.NameNotFoundException());
    when(mockedContext.getPackageName()
    ).thenReturn(packageName);
    when(mockedLogger.error(eq("TelemetryClientFactory"), contains("Failed when retrieving app meta-data: "))
    ).thenReturn(0);
  }
}