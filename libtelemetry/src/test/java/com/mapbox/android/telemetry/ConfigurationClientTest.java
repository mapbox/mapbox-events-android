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
import java.io.IOException;
import java.net.InetAddress;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_SHARED_PREFERENCES;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfigurationClientTest {
  private MockWebServer server;
  private ConfigurationClient configurationClient;
  private HandshakeCertificates serverCertificates;
  private HeldCertificate localhostCertificate;
  private HandshakeCertificates clientCertificates;

  @Before
  public void setUp() throws Exception {
    this.server = new MockWebServer();
    String localhost = InetAddress.getByName("localhost").getCanonicalHostName();
    localhostCertificate = new HeldCertificate.Builder()
      .addSubjectAlternativeName(localhost)
      .build();
    serverCertificates = new HandshakeCertificates.Builder()
      .heldCertificate(localhostCertificate)
      .build();
    clientCertificates = new HandshakeCertificates.Builder()
      .addTrustedCertificate(localhostCertificate.certificate())
      .build();
    server.useHttps(serverCertificates.sslSocketFactory(), false);
    server.start();

    TelemetryClientSettings settings = provideDefaultTelemetryClientSettings();
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    OkHttpClient client = settings.getClient(mockedBlacklist, 0);
    Context mockedContext = getConfigContext();

    File mockedFile = mock(File.class);
    FileOutputStream mockedOutputStream = mock(FileOutputStream.class);
    when(mockedContext.getFilesDir()).thenReturn(mockedFile);
    when(mockedContext.openFileOutput("MapboxBlacklist", Context.MODE_PRIVATE)).thenReturn(mockedOutputStream);

    SharedPreferences mockedSharedPreferences = mock(SharedPreferences.class);
    SharedPreferences.Editor mockedEditor = mock(SharedPreferences.Editor.class);
    when(mockedContext.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE))
      .thenReturn(mockedSharedPreferences);
    when(mockedSharedPreferences.getLong("mapboxConfigSyncTimestamp", 0))
      .thenReturn(Long.valueOf(0));
    when(mockedSharedPreferences.edit()).thenReturn(mockedEditor);

    this.configurationClient = new ConfigurationClient(mockedContext, TelemetryUtils.createFullUserAgent("AnUserAgent",
      mockedContext), "anAccessToken", client);
  }

  @After
  public void tearDown() throws Exception {
    server.shutdown();
  }

  @Test
  public void checkDaySinceLastUpdate() {
    assertTrue(configurationClient.shouldUpdate());
  }

  @Test
  public void successResponse() throws IOException {
    ConfigurationChangeHandler mockedHandler = mock(ConfigurationChangeHandler.class);
    configurationClient.addHandler(mockedHandler);
    Call mockedCall = mock(Call.class);
    Response response = getValidResponse();

    configurationClient.onResponse(mockedCall, response);

    verify(mockedHandler, times(1)).onUpdate("test");
  }

  @Test
  public void nullResponse() throws IOException {
    ConfigurationChangeHandler mockedHandler = mock(ConfigurationChangeHandler.class);
    configurationClient.addHandler(mockedHandler);
    Call mockedCall = mock(Call.class);

    configurationClient.onResponse(mockedCall, null);

    verify(mockedHandler, times(0)).onUpdate("test");
  }

  @Test
  public void nullHandler() throws Exception {
    ConfigurationChangeHandler nullHandler = null;
    configurationClient.addHandler(nullHandler);
    Call mockedCall = mock(Call.class);
    Response response = getValidResponse();

    configurationClient.onResponse(mockedCall, response);
  }

  private TelemetryClientSettings provideDefaultTelemetryClientSettings() {
    HttpUrl localUrl = obtainBaseEndpointUrl();

    return new TelemetryClientSettings.Builder(mock(Context.class))
      .baseUrl(localUrl)
      .sslSocketFactory(clientCertificates.sslSocketFactory())
      .x509TrustManager(clientCertificates.trustManager())
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

  private Response getValidResponse() throws IOException {
    Request request = mock(Request.class);

    Response.Builder builder = new Response.Builder();
    ResponseBody responseBody = mock(ResponseBody.class);
    when(responseBody.string()).thenReturn("test");

    return builder.request(request)
      .body(responseBody)
      .protocol(Protocol.HTTP_1_1)
      .code(200)
      .message("success")
      .build();
  }
}
