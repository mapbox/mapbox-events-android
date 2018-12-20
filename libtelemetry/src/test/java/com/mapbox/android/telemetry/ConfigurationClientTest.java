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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.internal.tls.SslClient;

import static com.mapbox.android.telemetry.TelemetryUtils.MAPBOX_SHARED_PREFERENCES;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationClientTest {
  private MockWebServer server;
  private ConfigurationClient configurationClient;

  @Before
  public void setUp() throws Exception {
    this.server = new MockWebServer();
    server.useHttps(SslClient.localhost().socketFactory, false);
    server.start();

    TelemetryClientSettings settings = provideDefaultTelemetryClientSettings();
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    OkHttpClient client = settings.getClient(mockedBlacklist);
    Context mockedContext = getConfigContext();

    File mockedFile = mock(File.class);
    FileOutputStream mockedOutputStream = mock(FileOutputStream.class);
    when(mockedContext.getFilesDir()).thenReturn(mockedFile);
    when(mockedContext.openFileOutput("MapboxBlacklist", Context.MODE_PRIVATE)).thenReturn(mockedOutputStream);

    SharedPreferences mockedSharedPreferences = mock(SharedPreferences.class);
    SharedPreferences.Editor mockedEditor = mock(SharedPreferences.Editor.class);
    when(mockedContext.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE))
      .thenReturn(mockedSharedPreferences);
    when(mockedSharedPreferences.getLong("mapboxConfigSyncTimestamp",0))
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
