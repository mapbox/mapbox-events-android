package com.mapbox.android.telemetry;


import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.List;

import okhttp3.Callback;

import static org.mockito.Mockito.mock;

public class TelemetryClientAppUserTurnstileEventTest extends MockWebServerTest {

  @Test
  public void sendsTheCorrectBodyPostingAppUserTurnstileEvent() throws Exception {
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    boolean indifferentTelemetryEnabled = false;
    Event anAppUserTurnstile = new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    List<Event> theAppUserTurnstile = obtainEvents(anAppUserTurnstile);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(theAppUserTurnstile, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), theAppUserTurnstile.get(0));
    assertRequestBodyEquals(expectedRequestBody);
  }
}
