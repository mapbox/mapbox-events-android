package com.mapbox.android.telemetry;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.internal.tls.SslClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CertificateBlacklistTest {
  private MockWebServer server;
  private CertificateBlacklist certificateBlacklist;

  @Before
  public void setUp() throws Exception {
    this.server = new MockWebServer();
    server.useHttps(SslClient.localhost().socketFactory, false);
    server.start();

    TelemetryClientSettings settings = provideDefaultTelemetryClientSettings();
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    OkHttpClient client = settings.getClient(mockedBlacklist);
    Context mockedContext = mock(Context.class);

    this.certificateBlacklist = new CertificateBlacklist(mockedContext, "anAccessToken",
      "AnUserAgent", client);
  }

  @Test
  public void checkDaySinceLastUpdate() throws Exception {
    assertTrue(certificateBlacklist.daySinceLastUpdate());
  }

  @Test
  public void checkRequestContainsUserAgentHeader() throws Exception {
    certificateBlacklist.requestBlacklist(obtainBaseEndpointUrl());

    assertRequestContainsHeader("User-Agent", "AnUserAgent");
  }

  private TelemetryClientSettings provideDefaultTelemetryClientSettings() {
    HttpUrl localUrl = obtainBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();

    return new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .hostnameVerifier(new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      })
      .build();
  }

  private HttpUrl obtainBaseEndpointUrl() {
    return server.url("/");
  }

  private void assertRequestContainsHeader(String key, String expectedValue) throws InterruptedException {
    assertRequestContainsHeader(key, expectedValue, 0);
  }

  private void assertRequestContainsHeader(String key, String expectedValue, int requestIndex)
    throws InterruptedException {
    RecordedRequest recordedRequest = obtainRecordedRequestAtIndex(requestIndex);
    String value = recordedRequest.getHeader(key);
    assertEquals(expectedValue, value);
  }

  private RecordedRequest obtainRecordedRequestAtIndex(int requestIndex) throws InterruptedException {
    RecordedRequest request = null;
    for (int i = 0; i <= requestIndex; i++) {
      request = server.takeRequest();
    }
    return request;
  }
}
