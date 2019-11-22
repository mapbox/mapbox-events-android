package com.mapbox.android.telemetry;

import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import static android.net.ConnectivityManager.TYPE_WIFI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TelemetryClientTest extends MockWebServerTest {

  private static final String REFORMED_USER_AGENT = "reformedUserAgent";

  @Test
  public void sendsContentTypeHeader() throws Exception {
    MapboxTelemetry.applicationContext = getMockedContext();
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent",
      REFORMED_USER_AGENT, MapboxTelemetry.applicationContext);
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback, false);

    assertRequestContainsHeader("Content-Type", "application/json; charset=utf-8");
  }

  @Test
  public void sendsContentEncodingHeader() throws Exception {
    MapboxTelemetry.applicationContext = getMockedContext();
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent",
      REFORMED_USER_AGENT, MapboxTelemetry.applicationContext);
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback, false);

    assertRequestContainsHeader("Content-Encoding", "gzip");
  }

  @Test
  public void sendsPostEventRequestWithTheCorrectAccessTokenParameter() throws Exception {
    MapboxTelemetry.applicationContext = getMockedContext();
    TelemetryClient telemetryClient = obtainATelemetryClient("theAccessToken", "anyUserAgent",
      REFORMED_USER_AGENT, MapboxTelemetry.applicationContext);
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback, false);

    assertRequestContainsParameter("access_token", "theAccessToken");
  }

  @Test
  public void sendsUserAgentHeader() throws Exception {
    MapboxTelemetry.applicationContext = getMockedContext();
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken",
      "theUserAgent", REFORMED_USER_AGENT, MapboxTelemetry.applicationContext);
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback, false);

    assertRequestContainsHeader("User-Agent", "theUserAgent");
  }

  @Test
  public void sendsReformedUserAgentHeader() throws Exception {
    MapboxTelemetry.applicationContext = getMockedContext();
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken",
      "theUserAgent", REFORMED_USER_AGENT, MapboxTelemetry.applicationContext);
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback, false);

    assertRequestContainsHeader("X-Mapbox-Agent", REFORMED_USER_AGENT);
  }

  @Test
  public void sendsPostEventRequestToTheCorrectEndpoint() throws Exception {
    MapboxTelemetry.applicationContext = getMockedContext();
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken",
      "anyUserAgent", REFORMED_USER_AGENT, MapboxTelemetry.applicationContext);
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback, false);

    assertPostRequestSentTo("/events/v2");
  }

  @Test
  public void sendsTheCorrectBodyPostingAnEvent() throws Exception {
    MapboxTelemetry.applicationContext = getMockedContext();
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken",
      "anyUserAgent", REFORMED_USER_AGENT, MapboxTelemetry.applicationContext);
    List<Event> theEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(theEvent, mockedCallback, false);

    ArrayList<Event> events = new ArrayList<>(1);
    events.add(theEvent.get(0));
    assertRequestBodyEquals(new Gson().toJson(events));
  }

  @Test
  public void receivesNoBodyPostingAnEventSuccessfully() throws Exception {
    MapboxTelemetry.applicationContext = getMockedContext();
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken",
      "anyUserAgent", REFORMED_USER_AGENT, MapboxTelemetry.applicationContext);
    List<Event> theEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockNoResponse(204);

    telemetryClient.sendEvents(theEvent, mockedCallback, false);

    assertResponseBodyEquals(null);
  }

  @Test
  public void parsesUnauthorizedRequestResponseProperlyPostingAnEvent() throws Exception {
    MapboxTelemetry.applicationContext = getMockedContext();
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken",
      "anyUserAgent", REFORMED_USER_AGENT, MapboxTelemetry.applicationContext);
    List<Event> theEvent = obtainAnEvent();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> bodyRef = new AtomicReference<>();
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback aCallback = provideACallback(latch, bodyRef, failureRef);
    enqueueMockResponse(401, "unauthorizedRequestResponse.json");

    telemetryClient.sendEvents(theEvent, aCallback, false);

    latch.await();
    assertTelemetryResponseEquals(bodyRef.get(), "Unauthorized request, usually because of "
      + "invalid access_token query parameter");
    assertFalse(failureRef.get());
  }

  @Test
  public void parsesInvalidMessageBodyResponseProperlyPostingAnEvent() throws Exception {
    MapboxTelemetry.applicationContext = getMockedContext();
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken",
      "anyUserAgent", REFORMED_USER_AGENT, MapboxTelemetry.applicationContext);
    List<Event> theEvent = obtainAnEvent();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> bodyRef = new AtomicReference<>();
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback aCallback = provideACallback(latch, bodyRef, failureRef);
    enqueueMockResponse(422, "invalidMessageBodyResponse.json");

    telemetryClient.sendEvents(theEvent, aCallback, false);

    latch.await();
    assertTelemetryResponseEquals(bodyRef.get(), "Invalid message body, check the types and required properties of "
      + "the events you sent");
    assertFalse(failureRef.get());
  }

  @Test
  public void checksRequestTimeoutFailure() throws Exception {
    Context mockedContext = getMockedContext();
    MapboxTelemetry.applicationContext = mockedContext;
    OkHttpClient localOkHttpClientWithShortTimeout = new OkHttpClient.Builder()
      .readTimeout(100, TimeUnit.MILLISECONDS)
      .build();
    HttpUrl localUrl = obtainBaseEndpointUrl();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder(mockedContext)
      .client(localOkHttpClientWithShortTimeout)
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
    String anyUserAgent = "anyUserAgent";
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", anyUserAgent, anyUserAgent,
      telemetryClientSettings,
      mock(Logger.class), mock(CertificateBlacklist.class));
    List<Event> theEvent = obtainAnEvent();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> bodyRef = new AtomicReference<>();
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback aCallback = provideACallback(latch, bodyRef, failureRef);
    enqueueMockNoResponse(504);

    telemetryClient.sendEvents(theEvent, aCallback, false);

    latch.await();
    assertTrue(failureRef.get());
  }

  @Test
  public void checksDebugLoggingEnabledBatch() throws Exception {
    Context mockedContext = getMockedContext();
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClientSettings clientSettings = provideDefaultTelemetryClientSettings(mockedContext);
    Logger mockedLogger = mock(Logger.class);
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    String anyUserAgent = "anyUserAgent";
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", anyUserAgent, anyUserAgent,
      clientSettings, mockedLogger, mock(CertificateBlacklist.class));
    telemetryClient.updateDebugLoggingEnabled(true);

    telemetryClient.sendEvents(mockedEvent, mockedCallback, false);

    verify(mockedLogger, times(1))
      .debug(eq("TelemetryClient"), contains(" with 1 event(s) (user agent: anyUserAgent) with payload:"));
  }

  @Test
  public void checksDebugLoggingEnabledAttachment() throws Exception {
    Context mockedContext = getMockedContext();
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClientSettings clientSettings = provideDefaultTelemetryClientSettings(mockedContext);
    Logger mockedLogger = mock(Logger.class);
    String anyUserAgent = "anyUserAgent";
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", anyUserAgent, anyUserAgent,
      clientSettings, mockedLogger, mock(CertificateBlacklist.class));
    telemetryClient.updateDebugLoggingEnabled(true);

    AttachmentListener attachmentListener = mock(AttachmentListener.class);
    CopyOnWriteArraySet<AttachmentListener> attachmentListeners = new CopyOnWriteArraySet<>();
    attachmentListeners.add(attachmentListener);

    saveFile(mockedContext, "test");
    telemetryClient.sendAttachment(createAttachment("test"), attachmentListeners);

    verify(mockedLogger, times(1))
      .debug(eq("TelemetryClient"), contains(" with 1 event(s) (user agent: anyUserAgent) with payload:"));
  }

  @Test
  public void checksSetBaseUrl() throws Exception {
    TelemetryClientSettings clientSettings = provideDefaultTelemetryClientSettings(getMockedContext());
    Logger mockedLogger = mock(Logger.class);
    TelemetryClient telemetryClient = new TelemetryClient("", "", "", clientSettings,
      mockedLogger, mock(CertificateBlacklist.class));

    String newUrl = "new-custom-url.com";
    telemetryClient.setBaseUrl(newUrl);

    assertEquals("https://" + newUrl + "/", telemetryClient.obtainSetting().getBaseUrl().toString());
  }


  private Callback provideACallback(final CountDownLatch latch, final AtomicReference<String> bodyRef,
                                    final AtomicBoolean failureRef) {
    Callback aCallback = new Callback() {
      @Override
      public void onFailure(Call call, IOException exception) {
        failureRef.set(true);
        latch.countDown();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        try {
          bodyRef.set(response.body().string());
        } catch (IOException exception) {
          throw exception;
        } finally {
          latch.countDown();
        }
      }
    };
    return aCallback;
  }

  private void assertTelemetryResponseEquals(String responseBody, String expectedMessage) {
    TelemetryResponse expectedTelemetryResponse = new TelemetryResponse(expectedMessage);
    TelemetryResponse actualTelemetryResponse = new Gson().fromJson(responseBody, TelemetryResponse.class);

    assertEquals(expectedTelemetryResponse, actualTelemetryResponse);
  }

  static Context getMockedContext() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    ConnectivityManager mockedCm = mock(ConnectivityManager.class, RETURNS_DEEP_STUBS);
    NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class, RETURNS_DEEP_STUBS);
    when(mockedNetworkInfo.getType()).thenReturn(TYPE_WIFI);
    when(mockedCm.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    when(mockedContext.getSystemService(anyString())).thenReturn(mockedCm);
    return mockedContext;
  }
}