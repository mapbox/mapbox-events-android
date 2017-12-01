package com.mapbox.services.android.telemetry;

import android.content.Context;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.internal.tls.SslClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TelemetryClientTest extends MockWebServerTest {

  @Test
  public void sendsContentTypeHeader() throws Exception {
    HttpUrl localUrl = getBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .build();
    Logger mockedLogger = mock(Logger.class);
    Context mockedContext = mock(Context.class);
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", telemetryClientSettings,
      mockedLogger, mockedContext);
    boolean indifferentTelemetryEnabled = false;
    Event mockedAppUserTurnstile =
      new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(mockedAppUserTurnstile, mockedCallback);

    assertRequestContainsHeader("Content-Type", "application/json; charset=utf-8");
  }

  @Test
  public void sendsContentEncodingHeader() throws Exception {
    HttpUrl localUrl = getBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .build();
    Logger mockedLogger = mock(Logger.class);
    Context mockedContext = mock(Context.class);
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", telemetryClientSettings,
      mockedLogger, mockedContext);
    boolean indifferentTelemetryEnabled = false;
    Event mockedAppUserTurnstile =
      new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(mockedAppUserTurnstile, mockedCallback);

    assertRequestContainsHeader("Content-Encoding", "gzip");
  }

  @Test
  public void sendsPostEventRequestWithTheCorrectAccessTokenParameter() throws Exception {
    HttpUrl localUrl = getBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .build();
    Logger mockedLogger = mock(Logger.class);
    Context mockedContext = mock(Context.class);
    TelemetryClient telemetryClient = new TelemetryClient("theAccessToken", "anyUserAgent", telemetryClientSettings,
      mockedLogger, mockedContext);
    boolean indifferentTelemetryEnabled = false;
    Event mockedAppUserTurnstile =
      new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(mockedAppUserTurnstile, mockedCallback);

    assertRequestContainsParameter("access_token", "theAccessToken");
  }

  @Test
  public void sendsUserAgentHeader() throws Exception {
    HttpUrl localUrl = getBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .build();
    Logger mockedLogger = mock(Logger.class);
    Context mockedContext = mock(Context.class);
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "theUserAgent", telemetryClientSettings,
      mockedLogger, mockedContext);
    boolean indifferentTelemetryEnabled = false;
    Event mockedAppUserTurnstile =
      new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(mockedAppUserTurnstile, mockedCallback);

    assertRequestContainsHeader("User-Agent", "theUserAgent");
  }

  @Test
  public void sendsPostEventRequestToTheCorrectEndpoint() throws Exception {
    HttpUrl localUrl = getBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .build();
    Logger mockedLogger = mock(Logger.class);
    Context mockedContext = mock(Context.class);
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", telemetryClientSettings,
      mockedLogger, mockedContext);
    boolean indifferentTelemetryEnabled = false;
    Event mockedAppUserTurnstile =
      new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(mockedAppUserTurnstile, mockedCallback);

    assertPostRequestSentTo("/events/v2");
  }

  @Test
  public void sendsTheCorrectBodyPostingAnEvent() throws Exception {
    HttpUrl localUrl = getBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .build();
    Logger mockedLogger = mock(Logger.class);
    Context mockedContext = mock(Context.class);
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", telemetryClientSettings,
      mockedLogger, mockedContext);
    boolean indifferentTelemetryEnabled = false;
    Event theAppUserTurnstile = new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(theAppUserTurnstile, mockedCallback);

    ArrayList<Event> events = new ArrayList<>(1);
    events.add(theAppUserTurnstile);
    assertRequestBodyEquals(new Gson().toJson(events));
  }

  @Test
  public void receivesNoBodyPostingAnEventSuccessfully() throws Exception {
    HttpUrl localUrl = getBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .build();
    Logger mockedLogger = mock(Logger.class);
    Context mockedContext = mock(Context.class);
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", telemetryClientSettings,
      mockedLogger, mockedContext);
    boolean indifferentTelemetryEnabled = false;
    Event theAppUserTurnstile = new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    Callback mockedCallback = mock(Callback.class);
    enqueueMockNoResponse(204);

    telemetryClient.sendEvent(theAppUserTurnstile, mockedCallback);

    assertResponseBodyEquals(null);
  }

  @Test
  public void parsesUnauthorizedRequestResponseProperlyPostingAnEvent() throws Exception {
    HttpUrl localUrl = getBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .build();
    Logger mockedLogger = mock(Logger.class);
    Context mockedContext = mock(Context.class);
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", telemetryClientSettings,
      mockedLogger, mockedContext);
    boolean indifferentTelemetryEnabled = false;
    Event theAppUserTurnstile = new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> bodyRef = new AtomicReference<>();
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback aCallback = provideACallback(latch, bodyRef, failureRef);
    enqueueMockResponse(401, "unauthorizedRequestResponse.json");

    telemetryClient.sendEvent(theAppUserTurnstile, aCallback);

    latch.await();
    assertTelemetryResponseEquals(bodyRef.get(), "Unauthorized request, usually because of "
      + "invalid access_token query parameter");
    assertFalse(failureRef.get());
  }

  @Test
  public void parsesInvalidMessageBodyResponseProperlyPostingAnEvent() throws Exception {
    HttpUrl localUrl = getBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .build();
    Logger mockedLogger = mock(Logger.class);
    Context mockedContext = mock(Context.class);
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", telemetryClientSettings,
      mockedLogger, mockedContext);
    boolean indifferentTelemetryEnabled = false;
    Event theAppUserTurnstile = new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> bodyRef = new AtomicReference<>();
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback aCallback = provideACallback(latch, bodyRef, failureRef);
    enqueueMockResponse(422, "invalidMessageBodyResponse.json");

    telemetryClient.sendEvent(theAppUserTurnstile, aCallback);

    latch.await();
    assertTelemetryResponseEquals(bodyRef.get(), "Invalid message body, check the types and required properties of "
      + "the events you sent");
    assertFalse(failureRef.get());
  }

  @Test
  public void checksRequestTimeoutFailure() throws Exception {
    OkHttpClient localOkHttpClientWithShortTimeout = new OkHttpClient.Builder()
      .readTimeout(100, TimeUnit.MILLISECONDS)
      .build();
    HttpUrl localUrl = getBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .client(localOkHttpClientWithShortTimeout)
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .build();
    Logger mockedLogger = mock(Logger.class);
    Context mockedContext = mock(Context.class);
    TelemetryClient telemetryClient = new TelemetryClient("anyAccessToken", "anyUserAgent", telemetryClientSettings,
      mockedLogger, mockedContext);
    boolean indifferentTelemetryEnabled = false;
    Event theAppUserTurnstile = new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> bodyRef = new AtomicReference<>();
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback aCallback = provideACallback(latch, bodyRef, failureRef);
    enqueueMockNoResponse(504);

    telemetryClient.sendEvent(theAppUserTurnstile, aCallback);

    latch.await();
    assertTrue(failureRef.get());
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