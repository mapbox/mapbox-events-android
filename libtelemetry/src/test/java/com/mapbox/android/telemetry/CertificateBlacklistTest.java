package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

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
import okhttp3.Callback;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CertificateBlacklistTest {
  private MockWebServer server;
  private CertificateBlacklist certificateBlacklist;
  private BlacklistClient blacklistClient;

  @Before
  public void setUp() throws Exception {
    this.server = new MockWebServer();
    server.useHttps(SslClient.localhost().socketFactory, false);
    server.start();

    Callback mockedCallback = mock(Callback.class);

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
      "AnUserAgent");
    this.blacklistClient = new BlacklistClient("AnUserAgent", client, mockedCallback);
  }

  @After
  public void tearDown() throws Exception {
    server.shutdown();
  }

  @Test
  public void checkDaySinceLastUpdate() {
    assertTrue(certificateBlacklist.daySinceLastUpdate());
  }

  @Test
  public void checkRequestContainsUserAgentHeader() throws Exception {
    blacklistClient.requestBlacklist(obtainBaseEndpointUrl());

    assertRequestContainsHeader("User-Agent", "AnUserAgent");
  }

  @Test
  public void checkBlacklistSaved() throws Exception {
    List<String> oneItemList = new ArrayList<>();
    oneItemList.add("sha256/test12345");

    assertFalse(certificateBlacklist.isBlacklisted(oneItemList.get(0)));

    Call mockedCall = mock(Call.class);
    Response mockedResponse = mock(Response.class);
    ResponseBody mockedBody = mock(ResponseBody.class);
    String fileContent = "{\"RevokedCertKeys\" : [\"test12345\"]}";

    when(mockedResponse.body()).thenReturn(mockedBody);
    when(mockedBody.string()).thenReturn(fileContent);

    certificateBlacklist.onResponse(mockedCall, mockedResponse);

    assertTrue(certificateBlacklist.isBlacklisted(oneItemList.get(0)));
  }

  @Test
  public void checkRequestUrlGeneration() throws Exception {
    Context mockedContext = getConfigContext();
    String accessToken = "accessToken";
    HttpUrl httpUrl = getHttpUrl(accessToken);

    assertEquals(httpUrl, blacklistClient.generateRequestUrl(mockedContext, accessToken));
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

  private HttpUrl getHttpUrl(String accessToken) {
    return new HttpUrl.Builder().scheme("https")
      .host("api.mapbox.com")
      .addPathSegment("events-config")
      .addQueryParameter("access_token", accessToken)
      .build();
  }

  private Context getConfigContext() throws PackageManager.NameNotFoundException {
    String anyAppInfoHostname = "any.app.info.hostname";
    String anyAppInfoAccessToken = "anyAppInfoAccessToken";
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    Bundle mockedBundle = mock(Bundle.class);
    when(mockedBundle.getString(eq("com.mapbox.TestEventsServer"))
    ).thenReturn(anyAppInfoHostname);
    when(mockedBundle.getString(eq("com.mapbox.TestEventsAccessToken"))
    ).thenReturn(anyAppInfoAccessToken);
    ApplicationInfo mockedApplicationInfo = mock(ApplicationInfo.class);
    mockedApplicationInfo.metaData = mockedBundle;
    String packageName = "com.foo.test";
    when(mockedContext.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA))
      .thenReturn(mockedApplicationInfo);
    when(mockedContext.getPackageName()).thenReturn(packageName);

    return mockedContext;
  }
}
