package com.mapbox.android.telemetry;

import android.content.Context;

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
import okhttp3.mockwebserver.internal.tls.SslClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TelemetryClientTest extends MockWebServerTest {

  @Test
  public void sendsContentTypeHeader() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback);

    assertRequestContainsHeader("Content-Type", "application/json; charset=utf-8");
  }

  @Test
  public void sendsContentEncodingHeader() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback);

    assertRequestContainsHeader("Content-Encoding", "gzip");
  }

  @Test
  public void sendsPostEventRequestWithTheCorrectAccessTokenParameter() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient telemetryClient = obtainATelemetryClient("theAccessToken", "anyUserAgent");
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback);

    assertRequestContainsParameter("access_token", "theAccessToken");
  }

  @Test
  public void sendsUserAgentHeader() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "theUserAgent");
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback);

    assertRequestContainsHeader("User-Agent", "theUserAgent");
  }

  @Test
  public void sendsPostEventRequestToTheCorrectEndpoint() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(mockedEvent, mockedCallback);

    assertPostRequestSentTo("/events/v2");
  }

  @Test
  public void sendsTheCorrectBodyPostingAnEvent() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    List<Event> theEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(theEvent, mockedCallback);

    ArrayList<Event> events = new ArrayList<>(1);
    events.add(theEvent.get(0));
    assertRequestBodyEquals(new Gson().toJson(events));
  }

  @Test
  public void receivesNoBodyPostingAnEventSuccessfully() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    List<Event> theEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);
    enqueueMockNoResponse(204);

    telemetryClient.sendEvents(theEvent, mockedCallback);

    assertResponseBodyEquals(null);
  }

  @Test
  public void parsesUnauthorizedRequestResponseProperlyPostingAnEvent() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    List<Event> theEvent = obtainAnEvent();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> bodyRef = new AtomicReference<>();
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback aCallback = provideACallback(latch, bodyRef, failureRef);
    enqueueMockResponse(401, "unauthorizedRequestResponse.json");

    telemetryClient.sendEvents(theEvent, aCallback);

    latch.await();
    assertTelemetryResponseEquals(bodyRef.get(), "Unauthorized request, usually because of "
      + "invalid access_token query parameter");
    assertFalse(failureRef.get());
  }

  @Test
  public void parsesInvalidMessageBodyResponseProperlyPostingAnEvent() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    List<Event> theEvent = obtainAnEvent();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> bodyRef = new AtomicReference<>();
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback aCallback = provideACallback(latch, bodyRef, failureRef);
    enqueueMockResponse(422, "invalidMessageBodyResponse.json");

    telemetryClient.sendEvents(theEvent, aCallback);

    latch.await();
    assertTelemetryResponseEquals(bodyRef.get(), "Invalid message body, check the types and required properties of "
      + "the events you sent");
    assertFalse(failureRef.get());
  }

  @Test
  public void checksRequestTimeoutFailure() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    OkHttpClient localOkHttpClientWithShortTimeout = new OkHttpClient.Builder()
      .readTimeout(100, TimeUnit.MILLISECONDS)
      .build();
    HttpUrl localUrl = obtainBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .client(localOkHttpClientWithShortTimeout)
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
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", telemetryClientSettings,
      mock(Logger.class), mock(CertificateBlacklist.class));
    List<Event> theEvent = obtainAnEvent();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> bodyRef = new AtomicReference<>();
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback aCallback = provideACallback(latch, bodyRef, failureRef);
    enqueueMockNoResponse(504);

    telemetryClient.sendEvents(theEvent, aCallback);

    latch.await();
    assertTrue(failureRef.get());
  }

  @Test
  public void checksDebugLoggingEnabledBatch() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClientSettings clientSettings = provideDefaultTelemetryClientSettings();
    Logger mockedLogger = mock(Logger.class);
    List<Event> mockedEvent = obtainAnEvent();
    Callback mockedCallback = mock(Callback.class);

    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", clientSettings,
      mockedLogger, mock(CertificateBlacklist.class));
    telemetryClient.updateDebugLoggingEnabled(true);

    telemetryClient.sendEvents(mockedEvent, mockedCallback);

    verify(mockedLogger, times(1))
      .debug(eq("TelemetryClient"), contains(" with 1 event(s) (user agent: anyUserAgent) with payload:"));
  }

  @Test
  public void checksDebugLoggingEnabledAttachment() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClientSettings clientSettings = provideDefaultTelemetryClientSettings();
    Logger mockedLogger = mock(Logger.class);

    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", clientSettings,
      mockedLogger, mock(CertificateBlacklist.class));
    telemetryClient.updateDebugLoggingEnabled(true);

    AttachmentListener attachmentListener = mock(AttachmentListener.class);
    CopyOnWriteArraySet<AttachmentListener> attachmentListeners = new CopyOnWriteArraySet<>();
    attachmentListeners.add(attachmentListener);

    saveFile(mockedContext, "test");
    telemetryClient.sendAttachment(createAttachment("test"), attachmentListeners);

    verify(mockedLogger, times(1))
      .debug(eq("TelemetryClient"), contains(" with 1 event(s) (user agent: anyUserAgent) with payload:"));
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
}