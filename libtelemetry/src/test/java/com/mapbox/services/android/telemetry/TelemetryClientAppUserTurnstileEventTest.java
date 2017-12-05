package com.mapbox.services.android.telemetry;


import com.google.gson.GsonBuilder;

import org.junit.Test;

import okhttp3.Callback;

import static org.mockito.Mockito.mock;

public class TelemetryClientAppUserTurnstileEventTest extends MockWebServerTest {

  @Test
  public void sendsTheCorrectBodyPostingAppUserTurnstileEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    boolean indifferentTelemetryEnabled = false;
    Event theAppUserTurnstile = new AppUserTurnstile(indifferentTelemetryEnabled, "anySdkIdentifier", "anySdkVersion");
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(theAppUserTurnstile, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), theAppUserTurnstile);
    assertRequestBodyEquals(expectedRequestBody);
  }
}
