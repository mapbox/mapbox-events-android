package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.internal.tls.SslClient;

import static com.mapbox.android.telemetry.CertificateBlacklist.MAPBOX_SHARED_PREFERENCE_KEY_BLACKLIST_TIMESTAMP;
import static com.mapbox.android.telemetry.TelemetryUtils.MAPBOX_SHARED_PREFERENCES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    File mockedFile = mock(File.class);
    FileOutputStream mockedOutputStream = mock(FileOutputStream.class);
    when(mockedContext.getFilesDir()).thenReturn(mockedFile);
    when(mockedContext.openFileOutput("MapboxBlacklist", Context.MODE_PRIVATE)).thenReturn(mockedOutputStream);

    SharedPreferences mockedSharedPreferences = mock(SharedPreferences.class);
    SharedPreferences.Editor mockedEditor = mock(SharedPreferences.Editor.class);
    when(mockedContext.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE))
      .thenReturn(mockedSharedPreferences);
    when(mockedSharedPreferences.getLong(MAPBOX_SHARED_PREFERENCE_KEY_BLACKLIST_TIMESTAMP,0))
      .thenReturn(Long.valueOf(0));
    when(mockedSharedPreferences.edit()).thenReturn(mockedEditor);

    this.certificateBlacklist = new CertificateBlacklist(mockedContext, "anAccessToken",
      "AnUserAgent", client);
  }

  @After
  public void tearDown() throws Exception {
    server.shutdown();
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

  @Test
  public void checkGetRevokedKeysSetAtInit() throws Exception {
    assertNotNull(certificateBlacklist.getRevokedKeys());
  }

  @Test
  public void CheckBlacklistSaved() throws Exception {
    List<String> emptyList = new ArrayList<>();
    List<String> oneItemList = new ArrayList<>();
    oneItemList.add("sha256/test12345");

    assertEquals(emptyList, certificateBlacklist.getRevokedKeys());

    Call mockedCall = mock(Call.class);
    Response mockedResponse = mock(Response.class);
    ResponseBody mockedBody = mock(ResponseBody.class);
    String fileContent = "{\"RevokedCertKeys\" : [\"test12345\"]}";

    when(mockedResponse.body()).thenReturn(mockedBody);
    when(mockedBody.string()).thenReturn(fileContent);

    certificateBlacklist.onResponse(mockedCall, mockedResponse);

    assertEquals(oneItemList, certificateBlacklist.getRevokedKeys());
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
